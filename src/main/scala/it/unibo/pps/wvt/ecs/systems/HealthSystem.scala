package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*



case class HealthSystem(elixirSystem: ElixirSystem, private val entitiesToRemove: Set[EntityId] = Set[EntityId]()) extends System:
  

  override def update(world: World): System =
    processDamageComponents(world)
      .processDeaths(world)
      .removeDeadEntities(world)

  // Process all DamageComponent in the world
  private def processDamageComponents(world: World): HealthSystem =
    val entitiesWithDamage = world.getEntitiesWithComponent[DamageComponent]
    entitiesWithDamage.foldLeft(this): (currentSystem, entityId) =>
      world.getComponent[DamageComponent](entityId) match
        case Some(damageComp) =>
          world.removeComponent[DamageComponent](entityId)
          currentSystem.applyDamageToEntity(world, entityId, damageComp)
        case None => currentSystem

  // Apply damage to a specific entity
  private def applyDamageToEntity(world: World, entityId: EntityId, damageComp: DamageComponent): HealthSystem =
    world.getComponent[HealthComponent](entityId) match
      case Some(healthComp) if healthComp.isAlive =>
        val newHealth = math.max(0, healthComp.currentHealth - damageComp.amount)
        val newHealthComp = healthComp.copy(currentHealth = newHealth)
        world.removeComponent[HealthComponent](entityId)
        world.addComponent(entityId, newHealthComp)
        if !newHealthComp.isAlive then
          giveElixirReward(world, entityId)
          markForRemoval(entityId)
        else this
      case _ => this

  // Handle entity deaths
  private def processDeaths(world: World): HealthSystem =
    val deadEntities = getNewlyDeadEntities(world)
    deadEntities.foldLeft(this): (currentSystem, entityId) =>
      currentSystem.markForRemoval(entityId)

  // Give elixir reward when entity dies
  private def giveElixirReward(world: World, deadEntityId: EntityId): Unit =
    val rewardAmount = calculateElixirReward(world, deadEntityId)
    if rewardAmount > 0 then
      elixirSystem.addElixir(rewardAmount)

  // Calculate elixir reward based on entity type
  private def calculateElixirReward(world: World, entityId: EntityId): Int =
    world.getComponent[TrollTypeComponent](entityId) match
      case Some(trollType) => trollType.trollType match
        case TrollType.Base => BASE_TROLL_REWARD
        case TrollType.Warrior => WARRIOR_TROLL_REWARD
        case TrollType.Assassin => ASSASSIN_TROLL_REWARD
        case TrollType.Thrower => THROWER_TROLL_REWARD
      case None => 0

  // Get newly dead entities
  private def getNewlyDeadEntities(world: World): List[EntityId] =
    world.getEntitiesWithComponent[HealthComponent].filter: entityId =>
      world.getComponent[HealthComponent](entityId).exists(!_.isAlive)
    .toList.filterNot(entitiesToRemove.contains)

  // Mark entity for removal
  private def markForRemoval(entityId: EntityId): HealthSystem =
    if entitiesToRemove.contains(entityId) then this
    else copy(entitiesToRemove = entitiesToRemove + entityId)

  // Remove dead entities from world
  private def removeDeadEntities(world: World): HealthSystem =
    entitiesToRemove.foreach: entityId =>
      if world.getAllEntities.contains(entityId) then
        world.destroyEntity(entityId)
    copy(entitiesToRemove = Set.empty)

  // Check if entity is alive
  def isAlive(world: World, entityId: EntityId): Boolean =
    world.getComponent[HealthComponent](entityId).exists(_.isAlive)

  // Get current health of entity
  def getCurrentHealth(world: World, entityId: EntityId): Option[Int] =
    world.getComponent[HealthComponent](entityId).map(_.currentHealth)

  // Get health percentage (0.0 - 1.0)
  def getHealthPercentage(world: World, entityId: EntityId): Option[Double] =
    world.getComponent[HealthComponent](entityId).map: healthComp =>
      if healthComp.maxHealth > 0 then
        healthComp.currentHealth.toDouble / healthComp.maxHealth.toDouble
      else 0.0

  // Create damage component for testing
  def createDamage(world: World, targetId: EntityId, damage: Int, sourceId: EntityId): Unit =
    world.addComponent(targetId, DamageComponent(damage, sourceId))