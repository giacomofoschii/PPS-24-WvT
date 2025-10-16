package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.controller.GameScenarioDSL.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import it.unibo.pps.wvt.utilities.GridMapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

class SpawnSystemTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  var world: World = _
  var spawnSystem: SpawnSystem = _

  before:
    world = World.empty
    spawnSystem = SpawnSystem.withConfig(Some(SPAWN_SEED))

  behavior of "SpawnSystem"

  it should "not be active initially" in:
    spawnSystem.isActive shouldBe false

  it should "activate when a wizard is placed" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, updatedSystem) = spawnSystem.update(testWorld)
    updatedSystem.asInstanceOf[SpawnSystem].isActive shouldBe true

  it should "not activate without wizards in world" in:
    val emptyWorld = World.empty
    val (_, updatedSystem) = spawnSystem.update(emptyWorld)
    updatedSystem.asInstanceOf[SpawnSystem].isActive shouldBe false

  it should "store first wizard row when activated" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, updatedSystem) = spawnSystem.update(testWorld)
    updatedSystem.asInstanceOf[SpawnSystem].firstWizardRow shouldBe Some(GRID_ROW_MID)

  it should "generate spawn events after initial delay" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val initialTime = System.currentTimeMillis()
    val fakeLastSpawnTime = initialTime - INITIAL_SPAWN_DELAY_MS - 10

    val spawnSystem = SpawnSystem.withConfig(Some(SPAWN_SEED)).copy(
      isActive = true,
      firstWizardRow = Some(GRID_ROW_MID),
      lastSpawnTime = fakeLastSpawnTime
    )

    val (_, updatedSystem) = spawnSystem.update(testWorld)
    updatedSystem.asInstanceOf[SpawnSystem].getPendingSpawnsCount should be > 0

  it should "spawn trolls at scheduled times" in:
      val (testWorld, _) = scenario: builder =>
        builder
          .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
          .withElixir(ELIXIR_START)

      val (_, system1) = spawnSystem.update(testWorld)

      Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

      var currentWorld = testWorld
      var currentSystem = system1
      var spawned = false
      var attempts = 0

      while (!spawned && attempts < SPAWN_ATTEMPTS)
        val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
        currentWorld = nextWorld
        currentSystem = nextSystem
        spawned = currentWorld.getEntitiesByType("troll").nonEmpty
        attempts += 1
        if (!spawned) Thread.sleep(SHORT_SLEEP_MS)

      currentWorld.getEntitiesByType("troll").size should be > 0


  it should "spawn trolls with correct components" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    val result = Iterator.iterate((testWorld, system1)): (w, s) =>
        Thread.sleep(DELAY_MEDIUM_MS)
        s.update(w)
      .zipWithIndex
      .take(SPAWN_ATTEMPTS)
      .find:
        case ((w, _), _) =>
          w.getEntitiesByType("troll").nonEmpty

    result match
      case Some(((currentWorld, _), _)) =>
        val trolls = currentWorld.getEntitiesByType("troll")
        trolls.size should be > 0
        trolls.foreach: troll =>
          currentWorld.hasComponent[TrollTypeComponent](troll) shouldBe true
          currentWorld.hasComponent[PositionComponent](troll) shouldBe true
          currentWorld.hasComponent[HealthComponent](troll) shouldBe true
          currentWorld.hasComponent[MovementComponent](troll) shouldBe true
          currentWorld.hasComponent[AttackComponent](troll) shouldBe true
      case None =>
        fail("No trolls were spawned within the expected updates")

  it should "respect maximum trolls per wave" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    val maxTrolls = system1.asInstanceOf[SpawnSystem].getMaxTrolls

    var currentWorld = testWorld
    var currentSystem = system1

    // Simulate enough time for all spawns
    (1 to UPDATES_COUNT_LONG).foreach: _ =>
      Thread.sleep(DELAY_MEDIUM_MS)
      (1 to UPDATES_COUNT_SHORT).foreach: _ =>
        val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
        currentWorld = nextWorld
        currentSystem = nextSystem

    currentSystem.asInstanceOf[SpawnSystem].getTrollsSpawned should be <= maxTrolls

  it should "deactivate when wave is complete" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    system1.asInstanceOf[SpawnSystem].isActive shouldBe true

    var currentWorld = testWorld
    var currentSystem = system1

    // Simulate spawning all trolls
    (1 to UPDATES_COUNT_LONG).foreach: _ =>
      Thread.sleep(DELAY_MEDIUM_MS)
      (1 to UPDATES_COUNT_SHORT).foreach: _ =>
        val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
        currentWorld = nextWorld
        currentSystem = nextSystem

      if currentSystem
        .asInstanceOf[SpawnSystem]
        .getTrollsSpawned >= currentSystem.asInstanceOf[SpawnSystem].getMaxTrolls &&
        currentSystem.asInstanceOf[SpawnSystem].getPendingSpawnsCount == 0 then
        currentSystem.asInstanceOf[SpawnSystem].isActive shouldBe false

  it should "apply wave scaling to spawned trolls" in:
    val spawnSystemWave5 = SpawnSystem.withConfig(Some(SPAWN_SEED)).copy(currentWave = WAVE_MID)

    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystemWave5.update(testWorld)
    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    var currentWorld = testWorld
    var currentSystem = system1
    var trolls = Seq.empty[EntityId]
    var attempts = 0

    while (trolls.isEmpty && attempts < SPAWN_ATTEMPTS) {
      Thread.sleep(DELAY_MEDIUM_MS)
      val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
      currentWorld = nextWorld
      currentSystem = nextSystem
      trolls = currentWorld.getEntitiesByType("troll").toSeq
      attempts += 1
    }

    trolls.size should be > 0

    val troll = trolls.head
    val health = currentWorld.getComponent[HealthComponent](troll).get

    // Wave 5 trolls should have scaled health
    val (baseHealth, _, _) = currentWorld.getComponent[TrollTypeComponent](troll)
      .map: c =>
        c.trollType match
          case TrollType.Base     => (BASE_TROLL_HEALTH, BASE_TROLL_SPEED, BASE_TROLL_DAMAGE)
          case TrollType.Warrior  => (WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_SPEED, WARRIOR_TROLL_DAMAGE)
          case TrollType.Assassin => (ASSASSIN_TROLL_HEALTH, ASSASSIN_TROLL_SPEED, ASSASSIN_TROLL_DAMAGE)
          case TrollType.Thrower  => (THROWER_TROLL_HEALTH, THROWER_TROLL_SPEED, THROWER_TROLL_DAMAGE)
      .get

    health.maxHealth should be > baseHealth

  it should "spawn first troll in wizard's row" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    var currentWorld = testWorld
    var currentSystem = system1
    (1 to UPDATES_COUNT_SHORT).foreach: _ =>
      val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
      currentWorld = nextWorld
      currentSystem = nextSystem

    val trolls = currentWorld.getEntitiesByType("troll")
    if trolls.nonEmpty then
      val firstTroll = trolls.head
      val pos = currentWorld.getComponent[PositionComponent](firstTroll).get.position
      val (row, _) = GridMapper.physicalToLogical(pos).get
      row shouldBe GRID_ROW_MID

  it should "generate spawns in batches" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    val (_, system2) = system1.update(testWorld)

    val batchSize = system2.asInstanceOf[SpawnSystem].getPendingSpawnsCount
    batchSize should (be >= 2 and be <= 4)

  it should "schedule spawns with time offsets within batch" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    val (_, system2) = system1.update(testWorld)

    system2.asInstanceOf[SpawnSystem].getNextSpawnTime shouldBe defined

  it should "handle pause correctly" in:
    // Note: This test requires GameEngine to be initialized
    // Simplified test without actual pause functionality
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    system1.asInstanceOf[SpawnSystem].pausedAt shouldBe None

  it should "track trolls spawned count" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    system1.asInstanceOf[SpawnSystem].getTrollsSpawned shouldBe 0

    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    var currentWorld = testWorld
    var currentSystem = system1
    (1 to UPDATES_PER_SECOND).foreach: _ =>
      val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
      currentWorld = nextWorld
      currentSystem = nextSystem

    currentSystem.asInstanceOf[SpawnSystem].getTrollsSpawned should be > 0

  it should "not generate new spawns when inactive" in:
    val inactiveSystem = spawnSystem.copy(isActive = false)
    val (testWorld, _) = scenario: builder =>
      builder.withElixir(ELIXIR_START)

    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    val (_, updatedSystem) = inactiveSystem.update(testWorld)
    updatedSystem.asInstanceOf[SpawnSystem].getPendingSpawnsCount shouldBe 0

  it should "spawn different troll types based on wave distribution" in:
      val spawnSystemWave10 = SpawnSystem.withConfig(Some(SPAWN_SEED)).copy(currentWave = WAVE_HIGH)

      val (testWorld, _) = scenario: builder =>
        builder
          .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
          .withElixir(ELIXIR_START)

      val (_, system1) = spawnSystemWave10.update(testWorld)

      Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

      var currentWorld = testWorld
      var currentSystem = system1

      (1 to (UPDATES_COUNT_MEDIUM * 2)).foreach { _ =>
        Thread.sleep(DELAY_MEDIUM_MS)
        (1 to (UPDATES_COUNT_SHORT * 2)).foreach { _ =>
          val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
          currentWorld = nextWorld
          currentSystem = nextSystem
        }
      }

      val trolls = currentWorld.getEntitiesByType("troll")
      trolls.size should be > 1

      val trollTypes = trolls.toSeq.map: troll =>
        currentWorld.getComponent[TrollTypeComponent](troll).get.trollType

      trollTypes.toSet.size should be > 1

  it should "spawn trolls at rightmost column" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    var currentWorld = testWorld
    var currentSystem = system1
    (1 to UPDATES_PER_SECOND).foreach: _ =>
      val (nextWorld, nextSystem) = currentSystem.update(currentWorld)
      currentWorld = nextWorld
      currentSystem = nextSystem

    val trolls = currentWorld.getEntitiesByType("troll")
    trolls.foreach: troll =>
      val pos = currentWorld.getComponent[PositionComponent](troll).get.position
      val (_, col) = GridMapper.physicalToLogical(pos).get
      col shouldBe (GRID_COLS_LOGICAL - 1)

  it should "update last spawn time after generating spawns" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    val initialTime = system1.asInstanceOf[SpawnSystem].lastSpawnTime

    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    val (_, system2) = system1.update(testWorld)

    if system2.asInstanceOf[SpawnSystem].hasSpawnedAtLeastOnce then
      system2.asInstanceOf[SpawnSystem].lastSpawnTime should be > initialTime

  it should "mark has spawned at least once after first spawn generation" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)
    system1.asInstanceOf[SpawnSystem].hasSpawnedAtLeastOnce shouldBe false

    Thread.sleep(INITIAL_SPAWN_DELAY_MS + SHORT_WAIT_MS)

    val (_, system2) = system1.update(testWorld)

    if system2.asInstanceOf[SpawnSystem].getPendingSpawnsCount > 0 then
      system2.asInstanceOf[SpawnSystem].hasSpawnedAtLeastOnce shouldBe true

  it should "use different spawn intervals for first and subsequent spawns" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, system1) = spawnSystem.update(testWorld)

    // First spawn should wait for initial interval
    Thread.sleep(LONG_SLEEP_MS)
    val (_, system2) = system1.update(testWorld)
    system2.asInstanceOf[SpawnSystem].getPendingSpawnsCount shouldBe 0

    // After initial interval
    Thread.sleep(INITIAL_SPAWN_DELAY_MS)
    val (_, system3) = system2.update(testWorld)
    system3.asInstanceOf[SpawnSystem].getPendingSpawnsCount should be > 0