package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*

import scala.util.Random
import scala.util.Random

case class SpawnEvent(
                     trollType: TrollType,
                     position: Position,
                     scheduledTime: Long
                     )

case class SpawnSystem(
                         private[systems] val spawnInterval: Long = 2000L,
                         private[systems] val lastSpawnTime: Long = System.currentTimeMillis(),
                         private[systems] val pendingSpawns: List[SpawnEvent] = List.empty,
                         private[systems] val rng: Random = Random(),
                         private[systems] val isActive: Boolean = false,
                         private[systems] val firstWizardRow: Option[Int] = None
                      ) extends System:

  type SpawnPredicate = (Position, World) => Boolean
  private type TrollSelector = Random => TrollType
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
      .map(_.position.row)

  private def processScheduledSpawns(world: World, currentTime: Long): SpawnSystem => SpawnSystem =
    system =>
      val (toSpawn, remaining) = system.pendingSpawns.partition(_.scheduledTime <= currentTime)

      toSpawn.foldLeft(world): (_, event) =>
        spawnTroll(event, world)
        world

      system.copy(pendingSpawns = remaining)

  private def generateNewSpawnsIfNeeded(world: World, currentTime: Long): SpawnSystem => SpawnSystem =
    system =>
      if shouldGenerateNewSpawn(system, currentTime) then
        val newSpawns = generateSpawnBatch(currentTime, system.firstWizardRow)
        system.copy(
          pendingSpawns = system.pendingSpawns ++ newSpawns,
          lastSpawnTime = currentTime
        )
      else
        system

  private def shouldGenerateNewSpawn(system: SpawnSystem, currentTime: Long): Boolean =
    currentTime - system.lastSpawnTime >= system.spawnInterval

  private def generateSpawnBatch(currentTime: Long, firstRow: Option[Int]): List[SpawnEvent] =
    val numOfSpawns = rng.nextInt(3) + 1

    val isFirstBatch = pendingSpawns.isEmpty && firstRow.isDefined

    (0 until numOfSpawns).map: i =>
      val useFirstRow = isFirstBatch && i == 0
      generateSingleSpawn(currentTime + i * 200, useFirstRow, firstRow)
    .toList

  private def generateSingleSpawn(baseTime: Long, useFirstRow: Boolean, firstRow: Option[Int]): SpawnEvent =
    SpawnEvent(
      trollType = selectTrollType(rng),
      position = generateSpawnPosition(rng, if useFirstRow then firstRow else None),
      scheduledTime = baseTime + rng.nextInt(500)
    )

  private val selectTrollType: TrollSelector = rng =>
    val weights = List(
      (Base, 50),
      (Warrior, 30),
      (Assassin, 15),
      (Thrower, 5)
    )

    val totalWeight = weights.map(_._2).sum
    val randomValue = rng.nextInt(totalWeight)

    var acc = 0
    weights.find: (tType, weight) =>
      acc += weight
      acc > randomValue
    .map(_._1).getOrElse(Base)

  private val generateSpawnPosition: PositionGenerator = (rng, fixedRow) =>
    val row = fixedRow.getOrElse(rng.nextInt(GRID_ROWS))
    val col = GRID_COLS - 1
    Position(row, col)

  private def spawnTroll(event: SpawnEvent, world: World): EntityId =
    event.trollType match
      case Base => EntityFactory.createBaseTroll(world, event.position)
      case Warrior => EntityFactory.createWarriorTroll(world, event.position)
      case Assassin => EntityFactory.createAssassinTroll(world, event.position)
      case Thrower => EntityFactory.createThrowerTroll(world, event.position)

  def getPendingSpawnsCount: Int =
    pendingSpawns.size

  def getNextSpawnTime: Option[Long] =
    pendingSpawns.minByOption(_.scheduledTime).map(_.scheduledTime)

  def withInterval(interval: Long): SpawnSystem =
    copy(spawnInterval = interval)

  def reset(): SpawnSystem =
    SpawnSystem(
      spawnInterval = spawnInterval,
      rng = rng,
      firstWizardRow = None
    )

object SpawnSystem:

  def withConfig(interval: Long = 2000L, seed: Option[Long] = None): SpawnSystem =
    val rng = seed.map(Random(_)).getOrElse(Random())
    SpawnSystem(spawnInterval = interval, rng = rng)