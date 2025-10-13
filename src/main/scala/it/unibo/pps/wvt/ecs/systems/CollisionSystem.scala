package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}

import scala.annotation.tailrec
import scala.util.Random

case class CollisionSystem(
                          private val random: Random = Random()
                          ) extends System:
  
  override def update(world: World): (World, System) =
    val world1 = processProjectileCollisions(world)
    val world2 = processMeleeCollisions(world1)
    val world3 = processBlockedTrolls(world2)
    (world3, this)
    
  private def processBlockedTrolls(world: World): World =
    val blockedTrolls = world.getEntitiesWithComponent[BlockedComponent].toList
    
    @tailrec
    def processBlocked(remaining: List[EntityId], currentWorld: World): World = 
      remaining match
        case Nil => currentWorld
        case troll :: tail =>
          val updatedWorld = world.getComponent[BlockedComponent](troll) match
            case Some(blocked) if !currentWorld.getAllEntities.contains(blocked.blockedBy) =>
              currentWorld.removeComponent[BlockedComponent](troll)
            case _ => currentWorld
          processBlocked(tail, updatedWorld)
        
    processBlocked(blockedTrolls, world)
    
  private def processProjectileCollisions(world: World): World =
    val projectiles = world.getEntitiesByType("projectile").toList
    
    @tailrec
    def processProjectileList(remaining: List[EntityId], currentWorld: World): World = 
      remaining match
        case Nil => currentWorld
        case projectile :: tail =>
          val updatedWorld = processProjectileCollision(projectile, currentWorld)
          processProjectileList(tail, updatedWorld)
        
    processProjectileList(projectiles, world)
    
  private def processProjectileCollision(projectile: EntityId, world: World): World =
    (for
      projPos <- world.getComponent[PositionComponent](projectile)
      projType <- world.getComponent[ProjectileTypeComponent](projectile)
      damage <- world.getComponent[DamageComponent](projectile)
    yield
      val targets = getValidTargets(projType.projectileType, world)
      findCollidingEntity(projPos.position, targets, world) match
        case Some(target) =>
          var updatedWorld = world
          
          if projType.projectileType == ProjectileType.Ice then
            updatedWorld = world.getComponent[FreezedComponent](target) match
              case Some(freezed) =>
                val newDuration = (freezed.remainingTime + 4000).min(10000)
                world.addComponent(target, FreezedComponent(newDuration, freezed.speedModifier))
              case None =>
                world.addComponent(target, FreezedComponent(4000, 0.5))
          
          updatedWorld
            .addComponent(target, CollisionComponent(damage.amount))
            .destroyEntity(projectile)
        case None => world
        
      ).getOrElse(world)
        
  private def getValidTargets(projType: ProjectileType, world: World): List[EntityId] =
    projType match
      case ProjectileType.Troll => world.getEntitiesByType("wizard").toList
      case _ => world.getEntitiesByType("troll").toList

  private def findCollidingEntity(position: Position, targets: List[EntityId], world: World): Option[EntityId] =
    val currentGrid = GridMapper.physicalToLogical(position)
    
    @tailrec
    def findCollision(remaining: List[EntityId]): Option[EntityId] = 
      remaining match
        case Nil => None
        case head :: tail =>
          world.getComponent[PositionComponent](head) match
            case Some(target) =>
              val targetGrid = GridMapper.physicalToLogical(target.position)
              if currentGrid == targetGrid then Some(head) else findCollision(tail)
            case None => findCollision(tail)
    
    currentGrid.flatMap(_ => findCollision(targets))
      
  private def processMeleeCollisions(world: World): World =
    val meleeTrolls = world.getEntitiesByType("troll").toList
    
    @tailrec
    def processMeleeList(remaining: List[EntityId], currentWorld: World): World =
      remaining match
        case Nil => currentWorld
        case troll :: tail =>
          val updatedWorld = processMeleeCollision(troll, currentWorld)
          processMeleeList(tail, updatedWorld)
        
    processMeleeList(meleeTrolls, world)
    
  private def processMeleeCollision(troll: EntityId, world: World): World =
    (for
      trollPos <- world.getComponent[PositionComponent](troll)
      trollType <- world.getComponent[TrollTypeComponent](troll)
      attack <- world.getComponent[AttackComponent](troll)
      if !isOnCooldown(troll, world)
    yield
      val wizards = world.getEntitiesByType("wizard").toList
      findCollidingEntity(trollPos.position, wizards, world) match
        case Some(wizard) =>
          var updatedWorld = world.addComponent(troll, BlockedComponent(wizard))
          if trollType.trollType != TrollType.Thrower then
            val damage = calculateMeleeDamage(trollType.trollType, attack.damage)
            
            updatedWorld = updatedWorld
              .addComponent(wizard, CollisionComponent(damage))
              .addComponent(troll, CooldownComponent(attack.cooldown))
            
          updatedWorld
        case None =>
          if world.hasComponent[BlockedComponent](troll) then
            world.removeComponent[BlockedComponent](troll)
          else world
      ).getOrElse(world)
      
  private def calculateMeleeDamage(trollType: TrollType, baseDamage: Int): Int =
    trollType match
      case TrollType.Assassin if random.nextDouble < 0.05 => baseDamage * 2
      case _ => baseDamage

  private def isOnCooldown(entity: EntityId, world: World): Boolean =
    world.getComponent[CooldownComponent](entity).exists(_.remainingTime > 0)