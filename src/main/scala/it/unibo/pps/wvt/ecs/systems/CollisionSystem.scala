package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}

import scala.annotation.tailrec
import scala.util.Random

case class CollisionSystem(
                          private val random: Random = Random()
                          ) extends System:
  
  override def update(world: World): System =
    processProjectileCollisions(world)
    processMeleeCollisions(world)
    processBlockedTrolls(world)
    this
    
  private def processBlockedTrolls(world: World): Unit =
    val blockedTrolls = world.getEntitiesWithComponent[BlockedComponent].toList
    
    @tailrec
    def processBlocked(remaining: List[EntityId]): Unit = remaining match
      case Nil => ()
      case troll :: tail =>
        world.getComponent[BlockedComponent](troll).foreach: blocked =>
          if !world.getAllEntities.contains(blocked.blockedBy) then
            world.removeComponent[BlockedComponent](troll)
        processBlocked(tail)
        
    processBlocked(blockedTrolls)
    
  private def processProjectileCollisions(world: World): Unit =
    val projectiles = world.getEntitiesByType("projectile").toList
    
    @tailrec
    def processProjectileList(remaining: List[EntityId]): Unit = remaining match
      case Nil => ()
      case projectile :: tail =>
        processProjectileCollision(projectile, world)
        processProjectileList(tail)
        
    processProjectileList(projectiles)
    
  private def processProjectileCollision(projectile: EntityId, world: World): Unit =
    for
      projPos <- world.getComponent[PositionComponent](projectile)
      projType <- world.getComponent[ProjectileTypeComponent](projectile)
      damage <- world.getComponent[DamageComponent](projectile)
    yield
      val targets = getValidTargets(projType.projectileType, world)
      findCollidingEntity(projPos.position, targets, world) match
        case Some(target) =>
          if projType.projectileType == ProjectileType.Ice then
            if world.hasComponent[FreezedComponent](target) then
              world.getComponent[FreezedComponent](target).foreach: freezed =>
                val newDuration = (freezed.remainingTime + 4000).min(10000)
                world.addComponent(target, FreezedComponent(newDuration, freezed.speedModifier))
            else
            world.addComponent(target, FreezedComponent(4000, 0.5))
          world.addComponent(target, CollisionComponent(damage.amount))
          world.destroyEntity(projectile)
        case None => ()
        
  private def getValidTargets(projType: ProjectileType, world: World): List[EntityId] =
    projType match
      case ProjectileType.Troll => world.getEntitiesByType("wizard").toList
      case _ => world.getEntitiesByType("troll").toList

  private def findCollidingEntity(position: Position, targets: List[EntityId], world: World): Option[EntityId] =
    val currentGrid = GridMapper.physicalToLogical(position)
    
    @tailrec
    def findCollision(remaining: List[EntityId]): Option[EntityId] = remaining match
      case Nil => None
      case head :: tail =>
        world.getComponent[PositionComponent](head) match
          case Some(target) =>
            val targetGrid = GridMapper.physicalToLogical(target.position)
            if currentGrid == targetGrid then Some(head) else findCollision(tail)
          case None => findCollision(tail)
    
    currentGrid.flatMap(_ => findCollision(targets))
      
  private def processMeleeCollisions(world: World): Unit =
    val meleeTrolls = world.getEntitiesByType("troll").toList
    
    @tailrec
    def processMeleeList(remaining: List[EntityId]): Unit = remaining match
      case Nil => ()
      case troll :: tail =>
        processMeleeCollision(troll, world)
        processMeleeList(tail)
        
    processMeleeList(meleeTrolls)
    
  private def processMeleeCollision(troll: EntityId, world: World): Unit =
    for
      trollPos <- world.getComponent[PositionComponent](troll)
      trollType <- world.getComponent[TrollTypeComponent](troll)
      attack <- world.getComponent[AttackComponent](troll)
      if !isOnCooldown(troll, world)
    yield
      val wizards = world.getEntitiesByType("wizard").toList
      findCollidingEntity(trollPos.position, wizards, world) match
        case Some(wizard) =>
          world.addComponent(troll, BlockedComponent(wizard))
          if trollType.trollType != TrollType.Thrower then
            val damage = calculateMeleeDamage(trollType.trollType, attack.damage)
            world.addComponent(wizard, CollisionComponent(damage))
            world.addComponent(troll, CooldownComponent(attack.cooldown))
        case None =>
          if world.hasComponent[BlockedComponent](troll) then
            world.removeComponent[BlockedComponent](troll)
      
  private def calculateMeleeDamage(trollType: TrollType, baseDamage: Int): Int =
    trollType match
      case TrollType.Assassin if random.nextDouble < 0.05 => baseDamage * 2
      case _ => baseDamage

  private def isOnCooldown(entity: EntityId, world: World): Boolean =
    world.getComponent[CooldownComponent](entity).exists(_.remainingTime > 0)