package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.config.WaveLevel
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

import scala.util.Random

case class SpawnEvent(
                       trollType: TrollType,
                       position: Position,
                       scheduledTime: Long
                     )

case class SpawnSystem(
                        private[systems] val lastSpawnTime: Long = System.currentTimeMillis(),
                        private[systems] val pendingSpawns: List[SpawnEvent] = List.empty,
                        private[systems] val rng: Random = Random(),
                        isActive: Boolean = false,
                        private[systems] val firstWizardRow: Option[Int] = None,
                        hasSpawnedAtLeastOnce: Boolean = false,
                        private[systems] val trollsSpawnedThisWave: Int = 0,
                        private[systems] val currentWave: Int = 1
                      ) extends System:

  private type TrollSelector = (Random, Int) => TrollType
  private type PositionGenerator = (Random, Option[Int]) => Position

  override def update(world: World): (World, System) =
    val currentTime = System.currentTimeMillis()

    val (world1, updatedSystem) =
      if !isActive && hasWizardBeenPlaced(world) then
        val wizardRow = getFirstWizardRow(world)
        (world, copy(isActive = true, firstWizardRow = wizardRow, lastSpawnTime = currentTime))
      else
        (world, this)

    if updatedSystem.isActive then
      val (world2, afterSpawn) = updatedSystem
        .processScheduledSpawns(world1, currentTime)

      val (world3, afterGenerate) = afterSpawn.generateNewSpawnsIfNeeded(world2, currentTime)

      val maxTrolls = WaveLevel.maxTrollsPerWave(afterGenerate.currentWave)
      if afterGenerate.trollsSpawnedThisWave >= maxTrolls && afterGenerate.pendingSpawns.isEmpty then
        (world3, afterGenerate.copy(isActive = false))
      else
        (world3, afterGenerate)
    else
      (world1, updatedSystem)

  private def hasWizardBeenPlaced(world: World): Boolean =
    world.getEntitiesByType("wizard").nonEmpty

  private def getFirstWizardRow(world: World): Option[Int] =
    world.getEntitiesByType("wizard")
      .headOption
      .flatMap(entity => world.getComponent[PositionComponent](entity))
      .flatMap(p => GridMapper.physicalToLogical(p.position).map(_._1))

  private def processScheduledSpawns(world: World, currentTime: Long): (World, SpawnSystem) =
      val (toSpawn, remaining) = pendingSpawns.partition(_.scheduledTime <= currentTime)
      val finalWorld = toSpawn.foldLeft(world): (currentWorld, event) =>
        val (updatedWorld, _) = spawnTroll(event, currentWorld)
        updatedWorld

      (finalWorld, copy(pendingSpawns = remaining))

  private def generateNewSpawnsIfNeeded(world: World, currentTime: Long): (World, SpawnSystem) =
    val maxTrolls = WaveLevel.maxTrollsPerWave(currentWave)
    if shouldGenerateNewSpawn(currentTime) && trollsSpawnedThisWave < maxTrolls then
      val remainingTrolls = maxTrolls - trollsSpawnedThisWave
      val numOfSpawns = Math.min(rng.nextInt(3) + 1, remainingTrolls)
      val newSpawns = generateSpawnBatch(currentTime, firstWizardRow, numOfSpawns)

      (world, copy(
        pendingSpawns = pendingSpawns ++ newSpawns,
        lastSpawnTime = currentTime,
        hasSpawnedAtLeastOnce = true,
        trollsSpawnedThisWave = trollsSpawnedThisWave + numOfSpawns
      ))
    else
      (world, this)

  private def shouldGenerateNewSpawn(currentTime: Long): Boolean =
    val interval = if !hasSpawnedAtLeastOnce then
      INITIAL_SPAWN_INTERVAL
    else
      WaveLevel.calculateSpawnInterval(currentWave)
    currentTime - lastSpawnTime >= interval

  private def generateSpawnBatch(currentTime: Long, firstRow: Option[Int], numOfSpawns: Int): List[SpawnEvent] =
    val isFirstBatch = pendingSpawns.isEmpty && firstRow.isDefined
    List.tabulate(numOfSpawns): index =>
      val useFirstRow = isFirstBatch && index == 0
      generateSingleSpawn(currentTime + index * BATCH_INTERVAL, useFirstRow, firstRow)

  private def generateSingleSpawn(baseTime: Long, useFirstRow: Boolean, firstRow: Option[Int]): SpawnEvent =
    SpawnEvent(
      trollType = selectTrollType(rng, currentWave),
      position = generateSpawnPosition(rng, if useFirstRow then firstRow else None),
      scheduledTime = baseTime + rng.nextInt(500)
    )

  private val selectTrollType: TrollSelector = (rng, currentWave) =>
    WaveLevel.selectRandomTrollType(WaveLevel.calculateTrollDistribution(currentWave))

  private val generateSpawnPosition: PositionGenerator = (rng, fixedRow) =>
    val row = fixedRow.getOrElse(rng.nextInt(GRID_ROWS))
    val col = GRID_COLS - 1
    GridMapper.logicalToPhysical(row, col).get

  private def spawnTroll(event: SpawnEvent, world: World): (World, EntityId) =
    val (world1, entity) = createTrollEntity(event, world)
    val world2 = applyWaveScaling(world1, entity, event.trollType)
    (world2, entity)

  private def createTrollEntity(event: SpawnEvent, world: World): (World, EntityId) =
    event.trollType match
      case Base => EntityFactory.createBaseTroll(world, event.position)
      case Warrior => EntityFactory.createWarriorTroll(world, event.position)
      case Assassin => EntityFactory.createAssassinTroll(world, event.position)
      case Thrower => EntityFactory.createThrowerTroll(world, event.position)

  private def applyWaveScaling(world: World, entity: EntityId, trollType: TrollType): World =
    val (baseHealth, baseSpeed, baseDamage) = getBaseStats(trollType)
    val (scaledHealth, scaledSpeed, scaledDamage) = WaveLevel.applyMultipliers(baseHealth, baseSpeed, baseDamage, currentWave)

    val world1 = updateHealth(world, entity, scaledHealth)
    val world2 = updateMovement(world1, entity, scaledSpeed)
    val world3 = updateAttack(world2, entity, scaledDamage)
    world3

  private def getBaseStats(trollType: TrollType): (Int, Double, Int) =
    trollType match
      case Base => (BASE_TROLL_HEALTH, BASE_TROLL_SPEED, BASE_TROLL_DAMAGE)
      case Warrior => (WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_SPEED, WARRIOR_TROLL_DAMAGE)
      case Assassin => (ASSASSIN_TROLL_HEALTH, ASSASSIN_TROLL_SPEED, ASSASSIN_TROLL_DAMAGE)
      case Thrower => (THROWER_TROLL_HEALTH, THROWER_TROLL_SPEED, THROWER_TROLL_DAMAGE)

  private def updateHealth(world: World, entity: EntityId, health: Int): World =
    world.getComponent[HealthComponent](entity) match
      case Some(_) =>
        world.updateComponent[HealthComponent](entity, _ => HealthComponent(health, health))
      case None => world

  private def updateMovement(world: World, entity: EntityId, speed: Double): World =
    world.getComponent[MovementComponent](entity) match
      case Some(_) =>
        world.updateComponent[MovementComponent](entity, _ => MovementComponent(speed))
      case None => world

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

object SpawnSystem:
  def withConfig(seed: Option[Long] = None): SpawnSystem =
    val rng = seed.map(Random(_)).getOrElse(Random())
    SpawnSystem(rng = rng)
