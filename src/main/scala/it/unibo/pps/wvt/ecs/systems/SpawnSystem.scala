package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.config.WaveLevel
import it.unibo.pps.wvt.engine.GameEngine
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

import scala.util.Random

/** Represents a scheduled spawn event for a troll.
  *
  * @param trollType     The type of troll to spawn.
  * @param position      The position where the troll will spawn.
  * @param scheduledTime The time at which the troll is scheduled to spawn.
  */
case class SpawnEvent(
    trollType: TrollType,
    position: Position,
    scheduledTime: Long
)

/** System responsible for the generation and the management of the trolls spawns during the waves
  *
  * @param lastSpawnTime          Timestamp of the last spawn generation
  * @param pendingSpawns          List of scheduled spawn events
  * @param rng                    Random number generator for spawn randomness
  * @param isActive               Indicates if the spawn system is currently active
  * @param firstWizardRow         The row where the first wizard was placed, if any
  * @param hasSpawnedAtLeastOnce  Flag to check if at least one spawn has occurred
  * @param trollsSpawnedThisWave  Count of trolls spawned in the current wave
  * @param currentWave            The current wave number
  * @param pausedAt               Timestamp when the game was paused, if applicable
  */
case class SpawnSystem(
    private[systems] val lastSpawnTime: Long = System.currentTimeMillis(),
    private[systems] val pendingSpawns: List[SpawnEvent] = List.empty,
    private[systems] val rng: Random = Random(),
    isActive: Boolean = false,
    private[systems] val firstWizardRow: Option[Int] = None,
    hasSpawnedAtLeastOnce: Boolean = false,
    private[systems] val trollsSpawnedThisWave: Int = 0,
    private[systems] val currentWave: Int = 1,
    private[systems] val pausedAt: Option[Long] = None
) extends System:

  private type TrollSelector     = (Random, Int) => TrollType
  private type PositionGenerator = (Random, Option[Int]) => Position

  override def update(world: World): (World, System) =
    val currentTime              = System.currentTimeMillis()
    val systemAfterPauseHandling = handlePauseResume(currentTime)

    val (world1, updatedSystem) =
      Option.when(!systemAfterPauseHandling.isActive && hasWizardBeenPlaced(world)):
        val wizardRow = getFirstWizardRow(world)
        (
          world,
          copy(
            isActive = true,
            firstWizardRow = wizardRow,
            lastSpawnTime = currentTime
          )
        )
      .getOrElse((world, systemAfterPauseHandling))

    Option.when(updatedSystem.isActive):
      val (world2, afterSpawn)    = updatedSystem.processScheduledSpawns(world1, currentTime)
      val (world3, afterGenerate) = afterSpawn.generateNewSpawnsIfNeeded(world2, currentTime)
      val maxTrolls               = WaveLevel.maxTrollsPerWave(afterGenerate.currentWave)
      if afterGenerate.trollsSpawnedThisWave >= maxTrolls && afterGenerate.pendingSpawns.isEmpty then
        (world3, afterGenerate.copy(isActive = false))
      else
        (world3, afterGenerate)
    .getOrElse((world1, updatedSystem))

  /** Handles the pause and resume functionality of the spawn system.
    *
    * @param currentTime The current system time in milliseconds.
    * @return An updated instance of SpawnSystem with adjusted timings if necessary.
    */
  private def handlePauseResume(currentTime: Long): SpawnSystem =
    val isPaused = GameEngine.getInstance.exists(_.isPaused)

    (isPaused, pausedAt) match
      case (true, None) =>
        copy(pausedAt = Some(currentTime))
      case (false, Some(pauseTime)) =>
        val pauseDuration = currentTime - pauseTime
        copy(
          lastSpawnTime = lastSpawnTime + pauseDuration,
          pendingSpawns = pendingSpawns.map(event =>
            event.copy(scheduledTime = event.scheduledTime + pauseDuration)
          ),
          pausedAt = None
        )
      case _ => this

  /** Checks if a wizard has been placed in the game world.
    *
    * @param world The current state of the game world.
    * @return True if at least one wizard entity exists, false otherwise.
    */
  private def hasWizardBeenPlaced(world: World): Boolean =
    world.getEntitiesByType("wizard").nonEmpty

  /** Retrieves the row of the first wizard entity in the game world.
    *
    * @param world The current state of the game world.
    * @return An Option containing the row index if a wizard exists, None otherwise.
    */
  private def getFirstWizardRow(world: World): Option[Int] =
    world.getEntitiesByType("wizard")
      .headOption
      .flatMap(entity => world.getComponent[PositionComponent](entity))
      .flatMap(p => GridMapper.physicalToLogical(p.position).map(_._1))

  /** Processes and spawns any scheduled trolls whose time has come.
    *
    * @param world       The current state of the game world.
    * @param currentTime The current system time in milliseconds.
    * @return A tuple containing the updated world and spawn system.
    */
  private def processScheduledSpawns(world: World, currentTime: Long): (World, SpawnSystem) =
    pausedAt
      .map(_ => (world, this))
      .getOrElse:
        val (toSpawn, remaining) = pendingSpawns.partition(_.scheduledTime <= currentTime)
        val finalWorld = toSpawn.foldLeft(world): (currentWorld, event) =>
          val (updatedWorld, _) = spawnTroll(event, currentWorld)
          updatedWorld

        (finalWorld, copy(pendingSpawns = remaining))

  /** Generates new spawn events if conditions are met, such as time intervals and wave limits.
    *
    * @param world       The current state of the game world.
    * @param currentTime The current system time in milliseconds.
    * @return A tuple containing the updated world and spawn system.
    */
  private def generateNewSpawnsIfNeeded(world: World, currentTime: Long): (World, SpawnSystem) =
    pausedAt
      .map(_ => (world, this))
      .getOrElse:
        val maxTrolls = WaveLevel.maxTrollsPerWave(currentWave)
        Option.when(shouldGenerateNewSpawn(currentTime) && trollsSpawnedThisWave < maxTrolls):
          val remainingTrolls = maxTrolls - trollsSpawnedThisWave
          val numOfSpawns = Math.min(
            rng.nextInt(MAX_BATCH_SIZE - BASE_BATCH_SIZE + 1) + BASE_BATCH_SIZE,
            remainingTrolls
          )
          val newSpawns = generateSpawnBatch(currentTime, firstWizardRow, numOfSpawns)
          (
            world,
            copy(
              pendingSpawns = pendingSpawns ++ newSpawns,
              lastSpawnTime = currentTime,
              hasSpawnedAtLeastOnce = true,
              trollsSpawnedThisWave = trollsSpawnedThisWave + numOfSpawns
            )
          )
        .getOrElse((world, this))

  /** Determines if a new spawn batch should be generated based on time intervals.
    *
    * @param currentTime The current system time in milliseconds.
    * @return True if a new spawn batch should be generated, false otherwise.
    */
  private def shouldGenerateNewSpawn(currentTime: Long): Boolean =
    val interval = if !hasSpawnedAtLeastOnce then
      INITIAL_SPAWN_INTERVAL
    else
      WaveLevel.calculateSpawnInterval(currentWave)
    currentTime - lastSpawnTime >= interval

  /** Generates a batch of spawn events.
    *
    * @param currentTime The current system time in milliseconds.
    * @param firstRow    The row of the first wizard, if any.
    * @param numOfSpawns The number of spawns to generate.
    * @return A list of generated SpawnEvent instances.
    */
  private def generateSpawnBatch(currentTime: Long, firstRow: Option[Int], numOfSpawns: Int): List[SpawnEvent] =
    val isFirstBatch = pendingSpawns.isEmpty && firstRow.isDefined
    List.tabulate(numOfSpawns): index =>
      val useFirstRow = isFirstBatch && index == 0
      generateSingleSpawn(currentTime + index * BATCH_INTERVAL, useFirstRow, firstRow)

  /** Generates a single spawn event.
    *
    * @param baseTime    The base time for scheduling the spawn.
    * @param useFirstRow Flag indicating whether to use the first wizard's row for spawning.
    * @param firstRow    The row of the first wizard, if any.
    * @return A generated SpawnEvent instance.
    */
  private def generateSingleSpawn(baseTime: Long, useFirstRow: Boolean, firstRow: Option[Int]): SpawnEvent =
    SpawnEvent(
      trollType = selectTrollType(rng, currentWave),
      position = generateSpawnPosition(rng, if useFirstRow then firstRow else None),
      scheduledTime = baseTime + rng.nextInt(500)
    )

  /** Selects a troll type based on the current wave and predefined distributions.
    *
    * @return A selected TrollType.
    */
  private val selectTrollType: TrollSelector = (rng, currentWave) =>
    WaveLevel.selectRandomTrollType(WaveLevel.calculateTrollDistribution(currentWave))

  /** Generates a spawn position for a troll, optionally using a fixed row.
    *
    * @return A generated Position for the troll spawn.
    */
  private val generateSpawnPosition: PositionGenerator = (rng, fixedRow) =>
    val row = fixedRow.getOrElse(rng.nextInt(GRID_ROWS))
    val col = GRID_COLS - 1
    GridMapper.logicalToPhysical(row, col).get

  /** Spawns a troll entity in the world based on the provided spawn event.
    *
    * @param event The spawn event containing troll type, position, and scheduled time.
    * @param world The current state of the game world.
    * @return A tuple containing the updated world and the ID of the spawned entity.
    */
  private def spawnTroll(event: SpawnEvent, world: World): (World, EntityId) =
    val (world1, entity) = createTrollEntity(event, world)
    val world2           = applyWaveScaling(world1, entity, event.trollType)
    (world2, entity)

  /** Creates a troll entity in the world based on the specified spawn event.
    *
    * @param event The spawn event containing troll type and position.
    * @param world The current state of the game world.
    * @return A tuple containing the updated world and the ID of the created entity.
    */
  private def createTrollEntity(event: SpawnEvent, world: World): (World, EntityId) =
    event.trollType match
      case Base     => EntityFactory.createBaseTroll(world, event.position)
      case Warrior  => EntityFactory.createWarriorTroll(world, event.position)
      case Assassin => EntityFactory.createAssassinTroll(world, event.position)
      case Thrower  => EntityFactory.createThrowerTroll(world, event.position)

  /** Applies wave-based scaling to the troll's attributes such as health, speed, and damage.
    *
    * @param world     The current state of the game world.
    * @param entity    The ID of the troll entity to scale.
    * @param trollType The type of the troll.
    * @return The updated world with the scaled troll entity.
    */
  private def applyWaveScaling(world: World, entity: EntityId, trollType: TrollType): World =
    val (baseHealth, baseSpeed, baseDamage) = getBaseStats(trollType)
    val (scaledHealth, scaledSpeed, scaledDamage) =
      WaveLevel.applyMultipliers(baseHealth, baseSpeed, baseDamage, currentWave)

    val world1 = updateHealth(world, entity, scaledHealth)
    val world2 = updateMovement(world1, entity, scaledSpeed)
    val world3 = updateAttack(world2, entity, scaledDamage)
    world3

  /** Retrieves the base stats for a given troll type.
    *
    * @param trollType The type of the troll.
    * @return A tuple containing the base health, speed, and damage to the troll.
    */
  private def getBaseStats(trollType: TrollType): (Int, Double, Int) =
    trollType match
      case Base     => (BASE_TROLL_HEALTH, BASE_TROLL_SPEED, BASE_TROLL_DAMAGE)
      case Warrior  => (WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_SPEED, WARRIOR_TROLL_DAMAGE)
      case Assassin => (ASSASSIN_TROLL_HEALTH, ASSASSIN_TROLL_SPEED, ASSASSIN_TROLL_DAMAGE)
      case Thrower  => (THROWER_TROLL_HEALTH, THROWER_TROLL_SPEED, THROWER_TROLL_DAMAGE)

  /** Updates the health component of a troll entity.
    *
    * @param world  The current state of the game world.
    * @param entity The ID of the troll entity to update.
    * @param health The new health value to set.
    * @return The updated world with the modified health component.
    */
  private def updateHealth(world: World, entity: EntityId, health: Int): World =
    world.getComponent[HealthComponent](entity) match
      case Some(_) =>
        world.updateComponent[HealthComponent](entity, _ => HealthComponent(health, health))
      case None => world

  /** Updates the movement component of a troll entity.
    *
    * @param world  The current state of the game world.
    * @param entity The ID of the troll entity to update.
    * @param speed  The new speed value to set.
    * @return The updated world with the modified movement component.
    */
  private def updateMovement(world: World, entity: EntityId, speed: Double): World =
    world.getComponent[MovementComponent](entity) match
      case Some(_) =>
        world.updateComponent[MovementComponent](entity, _ => MovementComponent(speed))
      case None => world

  /** Updates the attack component of a troll entity.
    *
    * @param world  The current state of the game world.
    * @param entity The ID of the troll entity to update.
    * @param damage The new damage value to set.
    * @return The updated world with the modified attack component.
    */
  private def updateAttack(world: World, entity: EntityId, damage: Int): World =
    world.getComponent[AttackComponent](entity) match
      case Some(old) =>
        world.updateComponent[AttackComponent](entity, _ => AttackComponent(damage, old.range, old.cooldown))
      case None => world

  def getPendingSpawnsCount: Int = pendingSpawns.size

  def getNextSpawnTime: Option[Long] =
    pendingSpawns.minByOption(_.scheduledTime).map(_.scheduledTime)

  def getTrollsSpawned: Int = trollsSpawnedThisWave

  def getMaxTrolls: Int = WaveLevel.maxTrollsPerWave(currentWave)

/** Companion object for SpawnSystem providing factory methods. */
object SpawnSystem:
  /** Creates a SpawnSystem instance with an optional seed for the random number generator.
    *
    * @param seed Optional seed for the random number generator to ensure reproducibility.
    * @return A new instance of SpawnSystem.
    */
  def withConfig(seed: Option[Long] = None): SpawnSystem =
    val rng = seed.map(Random(_)).getOrElse(Random())
    SpawnSystem(rng = rng)
