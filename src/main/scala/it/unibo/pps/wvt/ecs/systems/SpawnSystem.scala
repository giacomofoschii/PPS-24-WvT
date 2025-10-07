package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.config.WaveLevel
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

import scala.annotation.tailrec
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

  override def update(world: World): System =
    val currentTime = System.currentTimeMillis()
    val updatedSystem = if !isActive && hasWizardBeenPlaced(world) then
      val wizardRow = getFirstWizardRow(world)
      copy(isActive = true, firstWizardRow = wizardRow, lastSpawnTime = currentTime)
    else
      this

    if updatedSystem.isActive then
      updatedSystem
        .processScheduledSpawns(world, currentTime)
        .andThen(generateNewSpawnsIfNeeded(world, currentTime))
        .apply(updatedSystem)
    else
      updatedSystem

  private def hasWizardBeenPlaced(world: World): Boolean =
    world.getEntitiesByType("wizard").nonEmpty

  private def getFirstWizardRow(world: World): Option[Int] =
    world.getEntitiesByType("wizard")
      .headOption
      .flatMap(entity => world.getComponent[PositionComponent](entity))
      .map(p => GridMapper.physicalToLogical(p.position).get._1)

  private def processScheduledSpawns(world: World, currentTime: Long): SpawnSystem => SpawnSystem =
    system =>
      val (toSpawn, remaining) = system.pendingSpawns.partition(_.scheduledTime <= currentTime)

      @tailrec
      def spawnAll(events: List[SpawnEvent]): Unit =
        events match
          case Nil => ()
          case head :: tail =>
            spawnTroll(head, world)
            spawnAll(tail)

      spawnAll(toSpawn)
      system.copy(pendingSpawns = remaining)

  private def generateNewSpawnsIfNeeded(world: World, currentTime: Long): SpawnSystem => SpawnSystem =
    system =>
      val maxTrolls = WaveLevel.maxTrollsPerWave(currentWave)

      if shouldGenerateNewSpawn(system, currentTime) && system.trollsSpawnedThisWave < maxTrolls then
        val remainingTrolls = maxTrolls - system.trollsSpawnedThisWave
        val numOfSpawns = Math.min(rng.nextInt(3) + 1, remainingTrolls)
        val newSpawns = generateSpawnBatch(currentTime, system.firstWizardRow, numOfSpawns)

        system.copy(
          pendingSpawns = system.pendingSpawns ++ newSpawns,
          lastSpawnTime = currentTime,
          hasSpawnedAtLeastOnce = true,
          trollsSpawnedThisWave = system.trollsSpawnedThisWave + numOfSpawns
        )
      else
        system

  private def shouldGenerateNewSpawn(system: SpawnSystem, currentTime: Long): Boolean =
    currentTime - system.lastSpawnTime >= WaveLevel.calculateSpawnInterval(currentWave)

  private def generateSpawnBatch(currentTime: Long, firstRow: Option[Int], numOfSpawns: Int): List[SpawnEvent] =
    val isFirstBatch = pendingSpawns.isEmpty && firstRow.isDefined

    @tailrec
    def buildBatch(index: Int, acc: List[SpawnEvent]): List[SpawnEvent] =
      if index >= numOfSpawns then acc.reverse
      else
        val useFirstRow = isFirstBatch && index == 0
        val event = generateSingleSpawn(currentTime + index * 200, useFirstRow, firstRow)
        buildBatch(index + 1, event :: acc)

    buildBatch(0, List.empty)

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

  private def spawnTroll(event: SpawnEvent, world: World): EntityId =
    val entity = createTrollEntity(event, world)
    applyWaveScaling(entity, event.trollType, world)
    entity

  private def createTrollEntity(event: SpawnEvent, world: World): EntityId =
    event.trollType match
      case Base => EntityFactory.createBaseTroll(world, event.position)
      case Warrior => EntityFactory.createWarriorTroll(world, event.position)
      case Assassin => EntityFactory.createAssassinTroll(world, event.position)
      case Thrower => EntityFactory.createThrowerTroll(world, event.position)

  private def applyWaveScaling(entity: EntityId, trollType: TrollType, world: World): Unit =
    val (baseHealth, baseSpeed, baseDamage) = getBaseStats(trollType)
    val (scaledHealth, scaledSpeed, scaledDamage) = WaveLevel.applyMultipliers(baseHealth, baseSpeed, baseDamage, currentWave)

    updateHealth(entity, scaledHealth, world)
    updateMovement(entity, scaledSpeed, world)
    updateAttack(entity, scaledDamage, world)

  private def getBaseStats(trollType: TrollType): (Int, Double, Int) =
    trollType match
      case Base => (BASE_TROLL_HEALTH, BASE_TROLL_SPEED, BASE_TROLL_DAMAGE)
      case Warrior => (WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_SPEED, WARRIOR_TROLL_DAMAGE)
      case Assassin => (ASSASSIN_TROLL_HEALTH, ASSASSIN_TROLL_SPEED, ASSASSIN_TROLL_DAMAGE)
      case Thrower => (THROWER_TROLL_HEALTH, THROWER_TROLL_SPEED, THROWER_TROLL_DAMAGE)

  private def updateHealth(entity: EntityId, health: Int, world: World): Unit =
    world.getComponent[HealthComponent](entity).foreach: _ =>
      world.updateComponent[HealthComponent](entity, _ => HealthComponent(health, health))

  private def updateMovement(entity: EntityId, speed: Double, world: World): Unit =
    world.getComponent[MovementComponent](entity).foreach: _ =>
      world.updateComponent[MovementComponent](entity, _ => MovementComponent(speed))

  private def updateAttack(entity: EntityId, damage: Int, world: World): Unit =
    world.getComponent[AttackComponent](entity).foreach: old =>
      world.updateComponent[AttackComponent](entity, _ => AttackComponent(damage, old.range, old.cooldown))

  // Getter methods
  def getPendingSpawnsCount: Int = pendingSpawns.size

  def getNextSpawnTime: Option[Long] =
    pendingSpawns.minByOption(_.scheduledTime).map(_.scheduledTime)

  def getTrollsSpawned: Int = trollsSpawnedThisWave

  def getMaxTrolls: Int = WaveLevel.maxTrollsPerWave(currentWave)


object SpawnSystem:
  def withConfig(seed: Option[Long] = None): SpawnSystem =
    val rng = seed.map(Random(_)).getOrElse(Random())
    SpawnSystem(rng = rng)