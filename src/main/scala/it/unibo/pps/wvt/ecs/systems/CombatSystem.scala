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

  override def update(world: World): System =
    processRangedAttacks(world)
    updateComponentTimer(world, classOf[CooldownComponent], (t, _) => CooldownComponent(t))
    updateComponentTimer(world, classOf[FreezedComponent], (t, c) => FreezedComponent(t, c.speedModifier))
    this
  
  private def processRangedAttacks(world: World): Unit =
    processWizardProjectiles(world)
    processThrowerProjectiles(world)

  private def processWizardProjectiles(world: World): Unit =
    @tailrec
    def processWizardList(wizards: List[(EntityId, WizardType, Position, AttackComponent)]): Unit =
      wizards match
        case Nil => ()
        case (entity, wizardType, pos, attack) :: tail =>
          val projType = wizardType match
            case WizardType.Fire => ProjectileType.Fire
            case WizardType.Ice => ProjectileType.Ice
            case _ => ProjectileType.Wind
          EntityFactory.createProjectile(world, pos, projType)
          world.addComponent(entity, CooldownComponent(attack.cooldown))
          processWizardList(tail)

    val wizards = for
      entity <- world.getEntitiesByType("wizard")
      wizardType <- world.getComponent[WizardTypeComponent](entity)
      if wizardType.wizardType != WizardType.Generator && wizardType.wizardType != WizardType.Barrier
      pos <- world.getComponent[PositionComponent](entity)
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
      if hasTargetsInRange(entity, pos.position, attack.range, world)
    yield (entity, wizardType.wizardType, pos.position, attack)

    processWizardList(wizards.toList)

  private def hasTargetsInRange(wizardEntity: EntityId, wizardPos: Position, range: Double, world: World): Boolean =
    @tailrec
    def checkTrolls(trolls: List[EntityId]): Boolean =
      trolls match
        case Nil => false
        case head :: tail =>
          world.getComponent[PositionComponent](head) match
            case Some(trollPos) =>
              val distance = calculateDistance(wizardPos, trollPos.position)
              if distance <= range && distance >= 0 then true
              else checkTrolls(tail)
            case None => checkTrolls(tail)

    checkTrolls(world.getEntitiesByType("troll").toList)

  private def calculateDistance(pos1: Position, pos2: Position): Double =
    (GridMapper.physicalToLogical(pos1), GridMapper.physicalToLogical(pos2)) match
      case (Some((r1, c1)), Some((r2, c2))) if r1 == r2 =>
        (c2 - c1).toDouble
      case _ =>
        Double.MaxValue



  private def processThrowerProjectiles(world: World): Unit =
    @tailrec
    def processThrowerList(throwers: List[(EntityId, Position, AttackComponent)]): Unit =
      throwers match
        case Nil => ()
        case (entity, pos, attack) :: tail =>
          EntityFactory.createProjectile(world, pos, ProjectileType.Troll)
          world.addComponent(entity, CooldownComponent(attack.cooldown))
          processThrowerList(tail)

    val throwers = for
      entity <- world.getEntitiesByType("troll")
      trollType <- world.getComponent[TrollTypeComponent](entity)
      if trollType.trollType == Thrower
      pos <- world.getComponent[PositionComponent](entity)
      if GridMapper.physicalToLogical(pos.position).get._2 <= 6
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
    yield (entity, pos.position, attack)

    processThrowerList(throwers.toList)

  private def isOnCooldown(entity: EntityId, world: World): Boolean =
    world.getComponent[CooldownComponent](entity).exists(_.remainingTime > 0)

  private def updateComponentTimer[C <: Component](
                                                    world: World,
                                                    componentClass: Class[C],
                                                    recreate: (Long, C) => C
                                                  )(using ct: ClassTag[C]): Unit =
    @tailrec
    def updateList(entities: List[EntityId]): Unit =
      entities match
        case Nil => ()
        case head :: tail =>
          world.getComponent[C](head).foreach { comp =>
            val newTime = (comp.asInstanceOf[{ def remainingTime: Long }].remainingTime - 16L).max(0L)
            if newTime > 0 then
              world.addComponent(head, recreate(newTime, comp))
            else
              world.removeComponent[C](head)
          }
          updateList(tail)
    updateList(world.getEntitiesWithComponent[C].toList)
    