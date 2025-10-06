package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.Position

import scala.annotation.tailrec
import scala.util.Random

case class CombatSystem() extends System:

  type TargetSelector = (EntityId, World)
  type DamageModifier = Int => Int

  override def update(world: World): System =
    processMeleeAttacks(world)
    processRangedAttacks(world)
    updateCooldowns(world)
    this

  private def processMeleeAttacks(world: World): Unit =
    @tailrec
    def processMeleeList(attackers: List[(EntityId, Position, AttackComponent)]): Unit =
      attackers match
        case Nil => ()
        case (attacker, pos, attack) :: tail =>
          val targetGrid = pos.toGrid
          if targetGrid.col > 0 then
            val targetPos = Position(targetGrid.row, targetGrid.col - 1)
            world.getEntityAt(targetPos).foreach: target =>
              world.getComponent[WizardTypeComponent](target).foreach: _ =>
                val damage = calculateMeleeDamage(attacker, target, attack.damage, world)
                world.addComponent(target, CollisionComponent(damage))
                world.addComponent(attacker, CooldownComponent(attack.cooldown))
          processMeleeList(tail)

    val meleeAttackers = for
      entity <- world.getEntitiesByType("troll")
      trollType <- world.getComponent[TrollTypeComponent](entity)
      if trollType.trollType != Thrower
      pos <- world.getComponent[PositionComponent](entity)
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
    yield (entity, pos.position, attack)

    processMeleeList(meleeAttackers.toList)

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
              if distance <= range then true
              else checkTrolls(tail)
            case None => checkTrolls(tail)

    checkTrolls(world.getEntitiesByType("troll").toList)

  private def calculateDistance(pos1: Position, pos2: Position): Double =
    val grid1 = pos1.toGrid
    val grid2 = pos2.toGrid
    math.sqrt(math.pow(grid1.col - grid2.col, 2) + math.pow(grid1.row - grid2.row, 2))

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
      if pos.position.toGrid.col <= 6
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
    yield (entity, pos.position, attack)

    processThrowerList(throwers.toList)

  private def calculateMeleeDamage(attacker: EntityId, target: EntityId, baseDamage: Int,
                                   world: World): Int =
    val attackerModifiers = world.getComponent[TrollTypeComponent](attacker).map: troll =>
      troll.trollType match
        case Assassin if Random.nextDouble() < 0.05 => Seq(ASSASSIN_TROLL_DAMAGE * 2)
        case _ => Seq.empty
    .getOrElse(Seq.empty)
    attackerModifiers.foldLeft(baseDamage)(_ * _)

  private def isOnCooldown(entity: EntityId, world: World): Boolean =
    world.getComponent[CooldownComponent](entity).exists(_.remainingTime > 0)

  private def updateCooldowns(world: World): Unit =
    @tailrec
    def updateCooldownList(entities: List[EntityId]): Unit =
      entities match
        case Nil => ()
        case head :: tail =>
          world.getComponent[CooldownComponent](head).foreach: cooldown =>
            val newTime = (cooldown.remainingTime - 16L).max(0L)
            if newTime > 0 then
              world.addComponent(head, CooldownComponent(newTime))
            else
              world.removeComponent[CooldownComponent](head)
          updateCooldownList(tail)

    updateCooldownList(world.getEntitiesWithComponent[CooldownComponent].toList)