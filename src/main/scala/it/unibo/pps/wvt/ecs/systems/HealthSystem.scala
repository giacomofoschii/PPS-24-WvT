package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

/** System that manages the health of entities, processes damage from collisions,
  * handles entity deaths, and rewards elixir for defeating certain entities.
  *
  * @param elixirSystem The ElixirSystem instance to manage elixir rewards.
  * @param entitiesToRemove A set of EntityIds marked for removal due to death.
  */
case class HealthSystem(
    elixirSystem: ElixirSystem,
    private val entitiesToRemove: Set[EntityId] = Set.empty
) extends System:

  override def update(world: World): (World, System) =
    val (world1, system1) = processCollisionComponents(world)
    val (world2, system2) = system1.processDeaths(world1)
    val (world3, system3) = system2.removeDeadEntities(world2)
    (world3, system3)

  /** Processes all entities with CollisionComponent, applies damage to their HealthComponent,
    * and removes the CollisionComponent after processing.
    * @param world The current game world.
    * @return A tuple containing the updated world and the HealthSystem instance.
    */
  private def processCollisionComponents(world: World): (World, HealthSystem) =
    world.getEntitiesWithComponent[CollisionComponent]
      .foldLeft((world, this)): (acc, entityId) =>
        val (currentWorld, currentSystem) = acc
        currentWorld.getComponent[CollisionComponent](entityId)
          .map: collision =>
            val worldWithoutCollision = currentWorld.removeComponent[CollisionComponent](entityId)
            currentSystem.applyCollisionToEntity(worldWithoutCollision, entityId, collision)
          .getOrElse(acc)

  /** Applies the effects of a CollisionComponent to an entity's HealthComponent.
    * If the entity's health drops to zero or below, it is marked for removal.
    * @param world The current game world.
    * @param entityId The ID of the entity to apply the collision to.
    * @param collisionComp The CollisionComponent containing damage information.
    * @return A tuple containing the updated world and the HealthSystem instance.
    */
  private def applyCollisionToEntity(
      world: World,
      entityId: EntityId,
      collisionComp: CollisionComponent
  ): (World, HealthSystem) =
    world.getComponent[HealthComponent](entityId)
      .filter(_.isAlive)
      .map: healthComp =>
        val newHealth     = math.max(0, healthComp.currentHealth - collisionComp.amount)
        val newHealthComp = healthComp.copy(currentHealth = newHealth)
        val updatedWorld  = updateHealth(world, entityId, newHealthComp)
        handlePossibleDeath(updatedWorld, entityId, newHealthComp)
      .getOrElse((world, this))

  /** Updates the HealthComponent of an entity in the world.
    * @param world The current game world.
    * @param entityId The ID of the entity to update.
    * @param newHealthComp The new HealthComponent to set.
    * @return The updated game world with the new HealthComponent.
    */
  private def updateHealth(world: World, entityId: EntityId, newHealthComp: HealthComponent): World =
    world
      .removeComponent[HealthComponent](entityId)
      .addComponent(entityId, newHealthComp)

  /** Handles the death of an entity if its health is zero or below.
    * Rewards elixir if applicable and marks the entity for removal.
    * @param world The current game world.
    * @param entityId The ID of the entity to check for death.
    * @param healthComp The HealthComponent of the entity.
    * @return A tuple containing the updated world and the HealthSystem instance.
    */
  private def handlePossibleDeath(
      world: World,
      entityId: EntityId,
      healthComp: HealthComponent
  ): (World, HealthSystem) =
    if healthComp.isAlive then (world, this)
    else
      val (updatedWorld, updatedSystem) = giveElixirReward(world, entityId)
      (updatedWorld, updatedSystem.markForRemoval(entityId))

  /** Processes all entities that have died (health <= 0), marks them for removal,
    * and returns the updated world and HealthSystem instance.
    * @param world The current game world.
    * @return A tuple containing the updated world and the HealthSystem instance.
    */
  private def processDeaths(world: World): (World, HealthSystem) =
    val newlyDeadEntities = getNewlyDeadEntities(world)
    (world, newlyDeadEntities.foldLeft(this)(_.markForRemoval(_)))

  /** Calculates and awards elixir for defeating certain types of entities.
    * @param world The current game world.
    * @param deadEntityId The ID of the entity that has died.
    * @return A tuple containing the updated world and the HealthSystem instance.
    */
  private def giveElixirReward(world: World, deadEntityId: EntityId): (World, HealthSystem) =
    calculateElixirReward(world, deadEntityId) match
      case reward if reward > 0 =>
        (world, copy(elixirSystem = elixirSystem.addElixir(reward)))
      case _ => (world, this)

  /** Calculates the elixir reward based on the type of the defeated entity.
    * @param world The current game world.
    * @param entityId The ID of the defeated entity.
    * @return The amount of elixir to be rewarded.
    */
  private def calculateElixirReward(world: World, entityId: EntityId): Int =
    world.getComponent[TrollTypeComponent](entityId)
      .map(_.trollType)
      .map:
        case TrollType.Base     => BASE_TROLL_REWARD
        case TrollType.Warrior  => WARRIOR_TROLL_REWARD
        case TrollType.Assassin => ASSASSIN_TROLL_REWARD
        case TrollType.Thrower  => THROWER_TROLL_REWARD
      .getOrElse(0)

  /** Retrieves a list of entities that have died (health <= 0) and are not already marked for removal.
    * @param world The current game world.
    * @return A list of EntityIds that have newly died.
    */
  private def getNewlyDeadEntities(world: World): List[EntityId] =
    for
      entityId <- world.getEntitiesWithComponent[HealthComponent].toList
      if !entitiesToRemove.contains(entityId)
      health <- world.getComponent[HealthComponent](entityId).toList
      if !health.isAlive
    yield entityId

  /** Marks an entity for removal by adding its ID to the entitiesToRemove set.
    * @param entityId The ID of the entity to mark for removal.
    * @return The updated HealthSystem instance with the entity marked for removal.
    */
  private def markForRemoval(entityId: EntityId): HealthSystem =
    if entitiesToRemove.contains(entityId) then this
    else copy(entitiesToRemove = entitiesToRemove + entityId)

  /** Removes all entities marked for removal from the world, including cascading removals
    * of components that depend on the removed entities.
    * @param world The current game world.
    * @return A tuple containing the updated world and the HealthSystem instance with an empty removal set.
    */
  private def removeDeadEntities(world: World): (World, HealthSystem) =
    val worldWithCascadingRemovals = entitiesToRemove.foldLeft(world)(removeBlockedComponents)

    val updatedWorld = entitiesToRemove.intersect(worldWithCascadingRemovals.getAllEntities)
      .foldLeft(worldWithCascadingRemovals)((w, entity) => w.destroyEntity(entity))

    (updatedWorld, copy(entitiesToRemove = Set.empty))

  /** Removes BlockedComponent from entities that are blocked by the specified dead entity.
    * This handles cascading removals when an entity is destroyed.
    * @param world The current game world.
    * @param deadEntityId The ID of the entity that has been destroyed.
    * @return The updated game world with relevant BlockedComponents removed.
    */
  private def removeBlockedComponents(world: World, deadEntityId: EntityId): World =
    world.getEntitiesWithComponent[BlockedComponent]
      .filter(entity => world.getComponent[BlockedComponent](entity).exists(_.blockedBy == deadEntityId))
      .foldLeft(world)((w, entity) => w.removeComponent[BlockedComponent](entity))

  /** Checks if an entity is alive based on its HealthComponent.
    * @param world The current game world.
    * @param entityId The ID of the entity to check.
    * @return True if the entity is alive, false otherwise.
    */
  def isAlive(world: World, entityId: EntityId): Boolean =
    world.getComponent[HealthComponent](entityId).exists(_.isAlive)

  /** Retrieves the current health of an entity.
    * @param world The current game world.
    * @param entityId The ID of the entity to check.
    * @return An Option containing the current health if the entity has a HealthComponent, None otherwise.
    */
  def getCurrentHealth(world: World, entityId: EntityId): Option[Int] =
    world.getComponent[HealthComponent](entityId).map(_.currentHealth)
