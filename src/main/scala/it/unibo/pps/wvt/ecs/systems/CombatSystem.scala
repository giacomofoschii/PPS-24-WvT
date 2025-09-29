package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.Position

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
    val meleeAttackers = for
      entity <- world.getEntitiesByType("troll")
      trollType <- world.getComponent[TrollTypeComponent](entity)
      if trollType.trollType != Thrower
      pos <- world.getComponent[PositionComponent](entity)
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
    yield (entity, pos.position, attack)
    meleeAttackers.foreach: (attacker, pos, attack) =>
      if pos.col > 0 then
        val targetPos = Position(pos.row, pos.col - 1)
        world.getEntityAt(targetPos).foreach: target =>
          world.getComponent[WizardTypeComponent](target).foreach: _ =>
            val damage = calculateMeleeDamage(attacker, target, attack.damage, world)
            world.addComponent(target, CollisionComponent(damage))
            world.addComponent(attacker, CooldownComponent(attack.cooldown))

  private def processRangedAttacks(world: World): Unit =
    processWizardProjectiles(world)
    processThrowerProjectiles(world)

  private def processWizardProjectiles(world: World): Unit =
    val wizards = for
      entity <- world.getEntitiesByType("wizard")
      wizardType <- world.getComponent[WizardTypeComponent](entity)
      if wizardType.wizardType != WizardType.Generator && wizardType.wizardType != WizardType.Barrier
      pos <- world.getComponent[PositionComponent](entity)
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
      if hasTargetsInRange(entity, pos.position, attack.range, world)
    yield (entity, wizardType.wizardType, pos.position, attack)
    
    wizards.foreach: (entity, wizardType, pos, attack) =>
      val projType = wizardType match
          case WizardType.Fire => ProjectileType.Fire
          case WizardType.Ice => ProjectileType.Ice
          case _ => ProjectileType.Base
      val projectile = EntityFactory.createProjectile(world, pos, projType)
      world.addComponent(entity, CooldownComponent(attack.cooldown))
  
  private def hasTargetsInRange(wizardEntity: EntityId, wizardPos: Position, range: Double, world: World): Boolean =
    world.getEntitiesByType("troll").exists: trollEntity =>
      world.getComponent[PositionComponent](trollEntity) match
        case Some(trollPos) =>
          val distance = calculateDistance(wizardPos, trollPos.position)
          distance <= range
        case None => false
  
  private def calculateDistance(pos1: Position, pos2: Position): Double =
    math.sqrt(math.pow(pos1.col - pos2.col, 2) + math.pow(pos1.row - pos2.row, 2))

  private def processThrowerProjectiles(world: World): Unit =
    val throwers = for
      entity <- world.getEntitiesByType("troll")
      trollType <- world.getComponent[TrollTypeComponent](entity)
      if trollType.trollType == Thrower
      pos <- world.getComponent[PositionComponent](entity)
      if pos.position.col <= 6
      attack <- world.getComponent[AttackComponent](entity)
      if !isOnCooldown(entity, world)
    yield (entity, pos.position, attack)
    
    throwers.foreach: (entity, pos, attack) =>
      val projectile = EntityFactory.createProjectile(world, pos, ProjectileType.Troll)
      world.addComponent(entity, CooldownComponent(attack.cooldown))

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
    world.getEntitiesWithComponent[CooldownComponent].foreach: entity =>
      world.getComponent[CooldownComponent](entity).foreach: cooldown =>
        val newTime = (cooldown.remainingTime - 16L).max(0L)
        if newTime > 0 then
          world.addComponent(entity, CooldownComponent(newTime))
        else
          world.removeComponent[CooldownComponent](entity)