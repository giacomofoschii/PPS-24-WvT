package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}

import scala.annotation.tailrec
import scala.language.reflectiveCalls
import scala.reflect.ClassTag

case class CombatSystem() extends System:

  type TargetSelector = (EntityId, World)
  type DamageModifier = Int => Int

  override def update(world: World): (World, System) =
    val world1 = processRangedAttacks(world)
    val world2 = updateComponentTimer(world1, classOf[CooldownComponent], (t, _) => CooldownComponent(t))
    val world3 = updateComponentTimer(world2, classOf[FreezedComponent], (t, c) => FreezedComponent(t, c.speedModifier))
    (world3, this)
  
  private def processRangedAttacks(world: World): World =
    val world1 = processWizardProjectiles(world)
    val world2= processThrowerProjectiles(world1)
    world2

  private def processWizardProjectiles(world: World): World =
    @tailrec
    def processWizardList(wizards: List[(EntityId, WizardType, Position, AttackComponent)],
                          currentWorld: World): World =
      wizards match
        case Nil => currentWorld
        case (entity, wizardType, pos, attack) :: tail =>
          val projType = wizardType match
            case WizardType.Fire => ProjectileType.Fire
            case WizardType.Ice => ProjectileType.Ice
            case _ => ProjectileType.Wind
          val updatedWorld = spawnProjectileAndSetCooldown(currentWorld, entity, pos, projType, attack.cooldown)
          processWizardList(tail, updatedWorld)

    val wizards = (for
      entity <- world.getEntitiesByType("wizard")
      wizardType <- world.getComponent[WizardTypeComponent](entity)
      if wizardType.wizardType != WizardType.Generator && wizardType.wizardType != WizardType.Barrier
      pos <- world.getComponent[PositionComponent](entity)
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
      if hasTargetsInRange(pos.position, attack.range, "troll", world, false)
    yield (entity, wizardType.wizardType, pos.position, attack)).toList

    processWizardList(wizards, world)

  private def processThrowerProjectiles(world: World): World =
    @tailrec
    def processThrowerList(throwers: List[(EntityId, Position, AttackComponent)], currentWorld: World): World =
      throwers match
        case Nil => currentWorld
        case (entity, pos, attack) :: tail =>
          val updatedWorld = spawnProjectileAndSetCooldown(world, entity, pos, ProjectileType.Troll, attack.cooldown)
          processThrowerList(tail, updatedWorld)

    val throwers = (for
      entity <- world.getEntitiesByType("troll")
      trollType <- world.getComponent[TrollTypeComponent](entity)
      if trollType.trollType == Thrower
      pos <- world.getComponent[PositionComponent](entity)
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
      if hasTargetsInRange(pos.position, attack.range, "wizard", world, true)
    yield (entity, pos.position, attack)).toList

    processThrowerList(throwers, world)

  private def hasTargetsInRange(attackerPos: Position, range: Double,
                                targetType: String, world: World, attacksLeft: Boolean): Boolean =
    @tailrec
    def checkTargets(targets: List[EntityId]): Boolean =
      targets match
        case Nil => false
        case head :: tail =>
          world.getComponent[PositionComponent](head) match
            case Some(targetPos) =>
              val distance = calculateDistance(attackerPos, targetPos.position)
              val correctDirection = if attacksLeft then distance <= 0 else distance >= 0
              val inRange = math.abs(distance) <= range
              if inRange && correctDirection then true
              else checkTargets(tail)
            case None => checkTargets(tail)

    checkTargets(world.getEntitiesByType(targetType).toList)

  private def calculateDistance(pos1: Position, pos2: Position): Double =
    (GridMapper.physicalToLogical(pos1), GridMapper.physicalToLogical(pos2)) match
      case (Some((r1, c1)), Some((r2, c2))) if r1 == r2 =>
        (c2 - c1).toDouble
      case _ =>
        Double.MaxValue

  private def spawnProjectileAndSetCooldown(world: World, entity: EntityId, position: Position,
                                            projectileType: ProjectileType, cooldown: Long): World =
    val (world1, _) = EntityFactory.createProjectile(world, position, projectileType)
    world1.addComponent(entity, CooldownComponent(cooldown))

  private def isOnCooldown(entity: EntityId, world: World): Boolean =
    world.getComponent[CooldownComponent](entity).exists(_.remainingTime > 0)

  private def updateComponentTimer[C <: Component](
                                                    world: World,
                                                    componentClass: Class[C],
                                                    recreate: (Long, C) => C
                                                  )(using ct: ClassTag[C]): World =
    @tailrec
    def updateList(entities: List[EntityId], currentWorld: World): World =
      entities match
        case Nil => currentWorld
        case head :: tail =>
          val updatedWorld = world.getComponent[C](head) match
            case Some(comp) =>
              val newTime = (comp.asInstanceOf[{ def remainingTime: Long }].remainingTime - 16L).max(0L)
              if newTime > 0 then
                currentWorld.addComponent(head, recreate(newTime, comp))
              else
                currentWorld.removeComponent[C](head)
            case None => currentWorld
          updateList(tail, updatedWorld)
    updateList(world.getEntitiesWithComponent[C].toList, world)
    