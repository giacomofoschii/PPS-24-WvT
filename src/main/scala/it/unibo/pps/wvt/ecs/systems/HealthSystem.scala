package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*



case class HealthSystem(elixirSystem: ElixirSystem, private val entitiesToRemove: Set[EntityId] = Set[EntityId]()) extends System:
  

  override def update(world: World): System =
    processCollisionComponents(world)
      .processDeaths(world)
      .removeDeadEntities(world)

  /* Process all collision components to apply damage
   * Remove collision components after processing
   */
  private def processCollisionComponents(world: World): HealthSystem =
    val entitiesWithDamage = world.getEntitiesWithComponent[CollisionComponent]
    entitiesWithDamage.foldLeft(this): (currentSystem, entityId) =>
      world.getComponent[CollisionComponent](entityId) match
        case Some(collisionComp) =>
          world.removeComponent[CollisionComponent](entityId)
          currentSystem.applyCollisionToEntity(world, entityId, collisionComp)
        case None => currentSystem

  /* Apply collision damage to entity's health
   * If health drops to 0 or below, mark entity for removal and give elixir reward
   */
  private def applyCollisionToEntity(world: World, entityId: EntityId, collisionComp: CollisionComponent): HealthSystem =
    world.getComponent[HealthComponent](entityId) match
      case Some(healthComp) if healthComp.isAlive =>
        val newHealth = math.max(0, healthComp.currentHealth - collisionComp.amount)
        val newHealthComp = healthComp.copy(currentHealth = newHealth)
        world.removeComponent[HealthComponent](entityId)
        world.addComponent(entityId, newHealthComp)
        if !newHealthComp.isAlive then
          giveElixirReward(world, entityId).markForRemoval(entityId) 
        else this
      case _ => this

  /* Process deaths by identifying newly dead entities
   * Mark them for removal
   */
  private def processDeaths(world: World): HealthSystem =
    val deadEntities = getNewlyDeadEntities(world)
    deadEntities.foldLeft(this): (currentSystem, entityId) =>
      currentSystem.markForRemoval(entityId)

  /* Give elixir reward to player based on entity type
   * Only applies to trolls
   */
  private def giveElixirReward(world: World, deadEntityId: EntityId): HealthSystem =
    val rewardAmount = calculateElixirReward(world, deadEntityId)
    if rewardAmount > 0 then
      copy(elixirSystem = elixirSystem.addElixir(rewardAmount))
    else
      this

  /* Calculate elixir reward based on troll type
   * Returns 0 for non-troll entities
   */
  private def calculateElixirReward(world: World, entityId: EntityId): Int =
    world.getComponent[TrollTypeComponent](entityId) match
      case Some(trollType) => trollType.trollType match
        case TrollType.Base => BASE_TROLL_REWARD
        case TrollType.Warrior => WARRIOR_TROLL_REWARD
        case TrollType.Assassin => ASSASSIN_TROLL_REWARD
        case TrollType.Thrower => THROWER_TROLL_REWARD
      case None => 0

  private def getNewlyDeadEntities(world: World): List[EntityId] =
    world.getEntitiesWithComponent[HealthComponent].filter: entityId =>
      world.getComponent[HealthComponent](entityId).exists(!_.isAlive)
    .toList.filterNot(entitiesToRemove.contains)

  private def markForRemoval(entityId: EntityId): HealthSystem =
    if entitiesToRemove.contains(entityId) then this
    else copy(entitiesToRemove = entitiesToRemove + entityId)

  private def removeDeadEntities(world: World): HealthSystem =
    entitiesToRemove.foreach: entityId =>
      if world.getAllEntities.contains(entityId) then
        world.destroyEntity(entityId)
    copy(entitiesToRemove = Set.empty)

  def isAlive(world: World, entityId: EntityId): Boolean =
    world.getComponent[HealthComponent](entityId).exists(_.isAlive)

  def getCurrentHealth(world: World, entityId: EntityId): Option[Int] =
    world.getComponent[HealthComponent](entityId).map(_.currentHealth)

  def getHealthPercentage(world: World, entityId: EntityId): Option[Double] =
    world.getComponent[HealthComponent](entityId).map: healthComp =>
      if healthComp.maxHealth > 0 then
        healthComp.currentHealth.toDouble / healthComp.maxHealth.toDouble
      else 0.0

  def createCollision(world: World, targetId: EntityId, damage: Int): Unit =
    world.addComponent(targetId, CollisionComponent(damage))