package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

case class HealthSystem(
                         elixirSystem: ElixirSystem,
                         private val entitiesToRemove: Set[EntityId] = Set.empty
                       ) extends System:

  override def update(world: World): (World, System) =
    val (world1, system1) = processCollisionComponents(world)
    val (world2, system2) = system1.processDeaths(world1)
    val (world3, system3) = system2.removeDeadEntities(world2)
    (world3, system3)

  private def processCollisionComponents(world: World): (World, HealthSystem) =
    world.getEntitiesWithComponent[CollisionComponent]
      .foldLeft((world, this)): (acc, entityId) =>
        val (currentWorld, currentSystem) = acc
        currentWorld.getComponent[CollisionComponent](entityId)
          .map: collision =>
            val worldWithoutCollision = currentWorld.removeComponent[CollisionComponent](entityId)
            currentSystem.applyCollisionToEntity(worldWithoutCollision, entityId, collision)
          .getOrElse(acc)

  private def applyCollisionToEntity(world: World, entityId: EntityId,
                                     collisionComp: CollisionComponent): (World, HealthSystem) =
    world.getComponent[HealthComponent](entityId)
      .filter(_.isAlive)
      .map: healthComp =>
        val newHealth = math.max(0, healthComp.currentHealth - collisionComp.amount)
        val newHealthComp = healthComp.copy(currentHealth = newHealth)
        val updatedWorld = updateHealth(world, entityId, newHealthComp)
        handlePossibleDeath(updatedWorld, entityId, newHealthComp)
      .getOrElse((world, this))

  private def updateHealth(world: World, entityId: EntityId, newHealthComp: HealthComponent): World =
    world
      .removeComponent[HealthComponent](entityId)
      .addComponent(entityId, newHealthComp)

  private def handlePossibleDeath(world: World, entityId: EntityId,
                                  healthComp: HealthComponent): (World, HealthSystem) =
    if healthComp.isAlive then (world, this)
    else
      val (updatedWorld, updatedSystem) = giveElixirReward(world, entityId)
      (updatedWorld, updatedSystem.markForRemoval(entityId))

  private def processDeaths(world: World): (World, HealthSystem) =
    val newlyDeadEntities = getNewlyDeadEntities(world)
    (world, newlyDeadEntities.foldLeft(this)(_.markForRemoval(_)))

  private def giveElixirReward(world: World, deadEntityId: EntityId): (World, HealthSystem) =
    calculateElixirReward(world, deadEntityId) match
      case reward if reward > 0 =>
        (world, copy(elixirSystem = elixirSystem.addElixir(reward)))
      case _ => (world, this)

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

  private def removeDeadEntities(world: World): (World, HealthSystem) =
    val updatedWorld = entitiesToRemove
      .filter(world.getAllEntities.contains)
      .foldLeft(world)((w, entity) => w.destroyEntity(entity))

    (updatedWorld, copy(entitiesToRemove = Set.empty))

  def isAlive(world: World, entityId: EntityId): Boolean =
    world.getComponent[HealthComponent](entityId).exists(_.isAlive)

  def getCurrentHealth(world: World, entityId: EntityId): Option[Int] =
    world.getComponent[HealthComponent](entityId).map(_.currentHealth)
