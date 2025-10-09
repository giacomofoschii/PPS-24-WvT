package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

case class HealthSystem(elixirSystem: ElixirSystem,
                        private val entitiesToRemove: Set[EntityId] = Set.empty) extends System:

  override def update(world: World): System =
    processCollisionComponents(world)
      .processDeaths(world)
      .removeDeadEntities(world)
  
  private def processCollisionComponents(world: World): HealthSystem =
    world.getEntitiesWithComponent[CollisionComponent]
      .foldLeft(this): (system, entityId) =>
        world.getComponent[CollisionComponent](entityId)
          .map: collision =>
            world.removeComponent[CollisionComponent](entityId)
            system.applyCollisionToEntity(world, entityId, collision)
          .getOrElse(system)
  
  private def applyCollisionToEntity(world: World, entityId: EntityId,
                                     collisionComp: CollisionComponent): HealthSystem =
    world.getComponent[HealthComponent](entityId)
      .filter(_.isAlive)
      .map: healthComp =>
        val newHealth = math.max(0, healthComp.currentHealth - collisionComp.amount)
        val newHealthComp = healthComp.copy(currentHealth = newHealth)
        updateHealth(world, entityId, newHealthComp)
        handlePossibleDeath(world, entityId, newHealthComp)
      .getOrElse(this)
  
  private def updateHealth(world: World, entityId: EntityId, newHealthComp: HealthComponent): Unit =
    world.removeComponent[HealthComponent](entityId)
    world.addComponent(entityId, newHealthComp)
  
  private def handlePossibleDeath(world: World, entityId: EntityId,
                                  healthComp: HealthComponent): HealthSystem =
    if healthComp.isAlive then this
    else giveElixirReward(world, entityId).markForRemoval(entityId)
  
  private def processDeaths(world: World): HealthSystem =
    getNewlyDeadEntities(world).foldLeft(this)(_.markForRemoval(_))
  
  private def giveElixirReward(world: World, deadEntityId: EntityId): HealthSystem =
    calculateElixirReward(world, deadEntityId) match
      case reward if reward > 0 => copy(elixirSystem = elixirSystem.addElixir(reward))
      case _ => this
  
  private def calculateElixirReward(world: World, entityId: EntityId): Int =
    world.getComponent[TrollTypeComponent](entityId)
      .map(_.trollType)
      .map:
        case TrollType.Base => BASE_TROLL_REWARD
        case TrollType.Warrior => WARRIOR_TROLL_REWARD
        case TrollType.Assassin => ASSASSIN_TROLL_REWARD
        case TrollType.Thrower => THROWER_TROLL_REWARD
      .getOrElse(0)
  
  private def getNewlyDeadEntities(world: World): List[EntityId] =
    for
      entityId <- world.getEntitiesWithComponent[HealthComponent].toList
      if !entitiesToRemove.contains(entityId)
      health <- world.getComponent[HealthComponent](entityId).toList
      if !health.isAlive
    yield entityId
  
  private def markForRemoval(entityId: EntityId): HealthSystem =
    if entitiesToRemove.contains(entityId) then this
    else copy(entitiesToRemove = entitiesToRemove + entityId)
  
  private def removeDeadEntities(world: World): HealthSystem =
    entitiesToRemove
      .filter(world.getAllEntities.contains)
      .foreach(world.destroyEntity)
    copy(entitiesToRemove = Set.empty)
  
  def isAlive(world: World, entityId: EntityId): Boolean =
    world.getComponent[HealthComponent](entityId).exists(_.isAlive)

  def getCurrentHealth(world: World, entityId: EntityId): Option[Int] =
    world.getComponent[HealthComponent](entityId).map(_.currentHealth)