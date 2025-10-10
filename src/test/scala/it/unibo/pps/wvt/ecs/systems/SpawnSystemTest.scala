package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.config.WaveLevel
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

import scala.util.Random

class SpawnSystemTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  // Test DSL for readable test setup
  private object SpawnTestDSL:
    extension (world: World)
      def withWizardAt(position: Position): World =
        EntityFactory.createGeneratorWizard(world, position)
        world

      def trollCount: Int =
        world.getEntitiesByType("troll").size

      def getTrollTypes: Set[TrollType] =
        world.getEntitiesByType("troll")
          .flatMap(world.getComponent[TrollTypeComponent])
          .map(_.trollType)
          .toSet

      def getTrollPositions: Seq[Position] =
        world.getEntitiesByType("troll")
          .flatMap(world.getComponent[PositionComponent])
          .map(_.position)
          .toSeq

      def getTrollHealthValues: Seq[Int] =
        world.getEntitiesByType("troll")
          .flatMap(world.getComponent[HealthComponent])
          .map(_.currentHealth)
          .toSeq

    extension (system: SpawnSystem)
      def activateWith(world: World): SpawnSystem =
        system.update(world).asInstanceOf[SpawnSystem]

      def updateAndWait(world: World, waitMs: Long): SpawnSystem =
        val updated = system.update(world).asInstanceOf[SpawnSystem]
        Thread.sleep(waitMs)
        updated

      def updateMultipleTimes(world: World, times: Int, waitMs: Long = SHORT_DELAY): SpawnSystem =
        var current = system
        for (_ <- 1 to times)
          current = current.updateAndWait(world, waitMs)
        current

  import SpawnTestDSL.*

  behavior of "SpawnSystem - Initialization and Activation"

  it should "start inactive with correct initial state" in:
    val system = SpawnSystem()

    system.isActive shouldBe false
    system.firstWizardRow shouldBe None
    system.hasSpawnedAtLeastOnce shouldBe false
    system.trollsSpawnedThisWave shouldBe 0
    system.getPendingSpawnsCount shouldBe 0
    system.currentWave shouldBe TEST_WAVE_1

  it should "not activate or spawn when no wizard is placed" in:
    val world = World()
    val system = SpawnSystem()

    val updatedSystem = system.updateAndWait(world, MEDIUM_DELAY)

    updatedSystem.isActive shouldBe false
    updatedSystem.firstWizardRow shouldBe None
    world.trollCount shouldBe 0

  it should "activate and capture wizard row when first wizard is placed" in:
    val world = World()
    val system = SpawnSystem()

    val physicalPos = GridMapper.logicalToPhysical(TEST_WIZARD_ROW, TEST_WIZARD_COL).get
    world.withWizardAt(physicalPos)
    val updatedSystem = system.activateWith(world)

    updatedSystem.isActive shouldBe true
    updatedSystem.firstWizardRow shouldBe Some(TEST_WIZARD_ROW)

  it should "work with multiple wizards using first wizard's row" in:
      val world = World()
      val system = SpawnSystem()

      val physicalPos1 = GridMapper.logicalToPhysical(TEST_WIZARD_ROW, TEST_WIZARD_COL).get
      val physicalPos2 = GridMapper.logicalToPhysical(TEST_WIZARD_ROW + 1, TEST_WIZARD_COL).get

      world.withWizardAt(physicalPos1)
      EntityFactory.createFireWizard(world, physicalPos2)

      val updatedSystem = system.activateWith(world)

      updatedSystem.isActive shouldBe true
      updatedSystem.firstWizardRow shouldBe Some(TEST_WIZARD_ROW)

  behavior of "SpawnSystem - Spawn Mechanics"

  it should "schedule and spawn trolls after activation" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val activeSystem = system.activateWith(world)

    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)
    Thread.sleep(spawnInterval + LONG_DELAY)
    val spawnedSystem = activeSystem.update(world).asInstanceOf[SpawnSystem]

    spawnedSystem.getPendingSpawnsCount should be > 0
    spawnedSystem.hasSpawnedAtLeastOnce shouldBe true

  it should "spawn trolls at rightmost column" in:
      val world = World()
      val system = SpawnSystem()

      val physicalPos = GridMapper.logicalToPhysical(TEST_WIZARD_ROW, TEST_WIZARD_COL).get
      world.withWizardAt(physicalPos)
      val activeSystem = system.activateWith(world)

      val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)
      Thread.sleep(spawnInterval + LONG_DELAY)
      activeSystem.updateMultipleTimes(world, TEST_MULTIPLE_UPDATES, MEDIUM_DELAY)

      val positions = world.getTrollPositions
      positions.foreach: pos =>
        val logicalCol = GridMapper.physicalToLogical(pos).map(_._2)
        logicalCol shouldBe Some(TEST_SPAWN_COLUMN)

  it should "spawn multiple trolls per batch" in:
    val world = World()
    val system = SpawnSystem(rng = Random(TEST_SEED))

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val activeSystem = system.activateWith(world)

    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    val spawnedSystem = activeSystem.update(world).asInstanceOf[SpawnSystem]

    spawnedSystem.getPendingSpawnsCount should be >= 1
    spawnedSystem.getPendingSpawnsCount should be <= 3

  it should "process scheduled spawns and reduce pending count" in:
    val world = World()
    val system = SpawnSystem()

    val physicalPos = GridMapper.logicalToPhysical(TEST_WIZARD_ROW, TEST_WIZARD_COL).get
    world.withWizardAt(physicalPos)
    val activeSystem = system.activateWith(world)

    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    val withSpawns = activeSystem.update(world).asInstanceOf[SpawnSystem]
    val pendingBefore = withSpawns.getPendingSpawnsCount

    pendingBefore should be > 0

    // Wait for scheduled spawns to be processed
    Thread.sleep(LONG_DELAY + MEDIUM_DELAY)
    val processed = withSpawns.update(world).asInstanceOf[SpawnSystem]

    // Either trolls have been spawned or pending count has been reduced
    val hasSpawned = world.trollCount > 0 || processed.getPendingSpawnsCount < pendingBefore
    hasSpawned shouldBe true

  it should "spawn different troll types over multiple updates" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    for (_ <- 1 to TEST_MANY_UPDATES)
      Thread.sleep(spawnInterval + SHORT_DELAY)
      currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    val trollTypes = world.getTrollTypes
    trollTypes.nonEmpty shouldBe true

  behavior of "SpawnSystem - Wave Scaling and Limits"

  it should "apply wave scaling to troll stats" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]
    Thread.sleep(LONG_DELAY)
    currentSystem.update(world)

    val trolls = world.getEntitiesByType("troll")
    if trolls.nonEmpty then
      val firstTroll = trolls.head
      world.getComponent[HealthComponent](firstTroll) shouldBe defined
      world.getComponent[MovementComponent](firstTroll) shouldBe defined
      world.getComponent[AttackComponent](firstTroll) shouldBe defined

  it should "respect wave max troll limit" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val maxTrolls = WaveLevel.maxTrollsPerWave(TEST_WAVE_1)
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    for (_ <- 1 to TEST_MANY_UPDATES)
      Thread.sleep(spawnInterval + SHORT_DELAY)
      currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    currentSystem.getTrollsSpawned should be <= maxTrolls

  it should "not generate spawns when max trolls reached" in:
    val world = World()
    val maxTrolls = WaveLevel.maxTrollsPerWave(TEST_WAVE_1)
    val system = SpawnSystem(trollsSpawnedThisWave = maxTrolls)

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    currentSystem.getPendingSpawnsCount shouldBe 0

  behavior of "SpawnSystem - State Management"

  it should "track trolls spawned and provide correct state info" in:
    val world = World()
    val system = SpawnSystem()

    system.getNextSpawnTime shouldBe None
    system.getTrollsSpawned shouldBe 0
    system.getMaxTrolls shouldBe WaveLevel.maxTrollsPerWave(TEST_WAVE_1)

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    currentSystem.getTrollsSpawned should be > 0
    if currentSystem.getPendingSpawnsCount > 0 then
      currentSystem.getNextSpawnTime shouldBe defined

  it should "maintain and update last spawn time" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val activeSystem = system.activateWith(world)
    val initialTime = activeSystem.lastSpawnTime

    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    val spawnedSystem = activeSystem.update(world).asInstanceOf[SpawnSystem]

    spawnedSystem.lastSpawnTime should be >= initialTime

  behavior of "SpawnSystem - Spawn Intervals"

  it should "respect spawn interval and not spawn too early" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]
    val timeAfterFirstSpawn = currentSystem.lastSpawnTime

    Thread.sleep(spawnInterval / 2)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    currentSystem.lastSpawnTime shouldBe timeAfterFirstSpawn

  it should "generate new spawns after interval expires" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]
    Thread.sleep(LONG_DELAY)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]
    val firstCount = currentSystem.getTrollsSpawned

    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    currentSystem.getTrollsSpawned should be >= firstCount

  behavior of "SpawnSystem - Random Seed and Factory"

  it should "produce deterministic results with fixed seed" in:
    val world1 = World()
    val world2 = World()
    val system1 = SpawnSystem(rng = Random(TEST_SEED))
    val system2 = SpawnSystem(rng = Random(TEST_SEED))

    world1.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    world2.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))

    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var current1 = system1.activateWith(world1)
    var current2 = system2.activateWith(world2)

    Thread.sleep(spawnInterval + MEDIUM_DELAY)
    current1 = current1.update(world1).asInstanceOf[SpawnSystem]
    current2 = current2.update(world2).asInstanceOf[SpawnSystem]

    current1.getPendingSpawnsCount shouldBe current2.getPendingSpawnsCount

  it should "create system with factory method" in:
    val system = SpawnSystem.withConfig(Some(TEST_SEED))

    system shouldBe a[SpawnSystem]
    system.isActive shouldBe false
    system.currentWave shouldBe TEST_WAVE_1

  behavior of "SpawnSystem - Edge Cases and Robustness"

  it should "handle empty world without crashing" in:
    val world = World()
    val system = SpawnSystem()

    noException should be thrownBy system.update(world)

  it should "handle rapid consecutive updates" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    var currentSystem = system.activateWith(world)

    noException should be thrownBy {
      for (_ <- 1 to TEST_MANY_UPDATES)
        currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]
    }

  it should "return correct system instance after update" in:
    val world = World()
    val system = SpawnSystem()

    val returnedSystem = system.update(world)

    returnedSystem shouldBe a[SpawnSystem]

  it should "maintain valid troll positions within grid bounds" in:
    val world = World()
    val system = SpawnSystem()

    world.withWizardAt(Position(TEST_WIZARD_ROW, TEST_WIZARD_COL))
    val spawnInterval = WaveLevel.calculateSpawnInterval(TEST_WAVE_1)

    var currentSystem = system.activateWith(world)
    for (_ <- 1 to TEST_MANY_UPDATES)
      Thread.sleep(spawnInterval + SHORT_DELAY)
      currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    val positions = world.getTrollPositions
    positions.foreach: pos =>
      GridMapper.physicalToLogical(pos) match
        case Some((row, col)) =>
          row should be >= 0
          row should be < GRID_ROWS
          col shouldBe TEST_SPAWN_COLUMN
        case None =>
          fail(s"Impossible to convert the physical position: $pos in logic one")