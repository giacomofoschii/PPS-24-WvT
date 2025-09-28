package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.TestConstants._
import it.unibo.pps.wvt.utilities.ViewConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class SpawnSystemTest extends AnyFlatSpec with Matchers:

  "SpawnSystem" should "start inactive" in:
    val system = SpawnSystem()
    system.isActive shouldBe false
    system.firstWizardRow shouldBe None

  it should "not spawn trolls when no wizard is placed" in:
    val world = World()
    val system = SpawnSystem(spawnInterval = SPAWN_INTERVAL_SHORT)

    Thread.sleep(SLEEP_BEFORE_UPDATE)
    val updatedSystem = system.update(world).asInstanceOf[SpawnSystem]

    updatedSystem.isActive shouldBe false
    world.getEntitiesByType("troll") shouldBe empty

  it should "activate when first wizard is placed" in:
    val world = World()
    val system = SpawnSystem()

    // Place a wizard
    val wizard = EntityFactory.createGeneratorWizard(world, Position(2, 3))

    val updatedSystem = system.update(world).asInstanceOf[SpawnSystem]

    updatedSystem.isActive shouldBe true
    updatedSystem.firstWizardRow shouldBe Some(2)

  it should "spawn trolls at rightmost column" in:
    val world = World()
    val system = SpawnSystem(spawnInterval = SPAWN_INTERVAL_SHORT)

    val wizard = EntityFactory.createWindWizard(world, Position(1, 1))

    val activeSystem = system.update(world).asInstanceOf[SpawnSystem]
    Thread.sleep(SLEEP_AFTER_SPAWN)
    val spawnedSystem = activeSystem.update(world).asInstanceOf[SpawnSystem]
    Thread.sleep(SPAWN_INTERVAL_SHORT)
    spawnedSystem.update(world)

    val trolls = world.getEntitiesByType("troll")
    trolls.foreach: troll =>
      val pos = world.getComponent[PositionComponent](troll)
      pos.map(_.position.col) shouldBe Some(GRID_COLS - 1)

  it should "spawn multiple trolls per batch" in:
    val world = World()
    val rng = Random(42) // Fixed seed for predictable test
    val system = SpawnSystem(spawnInterval = SPAWN_INTERVAL_SHORT, rng = rng)

    val wizard = EntityFactory.createFireWizard(world, Position(0, 0))

    val activeSystem = system.update(world).asInstanceOf[SpawnSystem]
    Thread.sleep(SLEEP_AFTER_SPAWN)
    val spawnedSystem = activeSystem.update(world).asInstanceOf[SpawnSystem]

    // Should have 1-3 pending spawns
    spawnedSystem.getPendingSpawnsCount should be >= 1
    spawnedSystem.getPendingSpawnsCount should be <= 3

  it should "spawn different troll types" in:
    val world = World()
    val system = SpawnSystem(spawnInterval = SPAWN_INTERVAL_SHORT)

    val wizard = EntityFactory.createIceWizard(world, Position(2, 2))

    var currentSystem = system.update(world).asInstanceOf[SpawnSystem]

    // Generate multiple spawn batches
    for (_ <- 1 to 10)
      Thread.sleep(SLEEP_BETWEEN_BATCHES)
      currentSystem = currentSystem.update(world).asInstanceOf[SpawnSystem]

    val trolls = world.getEntitiesByType("troll")
    val trollTypes = trolls.flatMap(world.getComponent[TrollTypeComponent](_))
      .map(_.trollType)
      .toSet

    // Should have spawned at least 2 different types
    trollTypes.size should be >= 2

  it should "reset properly" in:
    val world = World()
    val system = SpawnSystem(spawnInterval = SPAWN_INTERVAL_SHORT)

    val wizard = EntityFactory.createGeneratorWizard(world, Position(1, 1))

    val activeSystem = system.update(world).asInstanceOf[SpawnSystem]
    activeSystem.isActive shouldBe true
    activeSystem.firstWizardRow shouldBe Some(1)

    val resetSystem = activeSystem.reset()
    resetSystem.isActive shouldBe false
    resetSystem.firstWizardRow shouldBe None
    resetSystem.getPendingSpawnsCount shouldBe 0

  it should "handle scheduled spawn events correctly" in:
    val world = World()
    val system = SpawnSystem()

    system.getNextSpawnTime shouldBe None
    system.getPendingSpawnsCount shouldBe 0

  it should "maintain spawn interval after modification" in:
    val world = World()
    val system = SpawnSystem(spawnInterval = SPAWN_INTERVAL_LONG)
    val modifiedSystem = system.withInterval(SPAWN_INTERVAL_MEDIUM)

    modifiedSystem.spawnInterval shouldBe SPAWN_INTERVAL_MEDIUM

  "SpawnSystem factory" should "create system with custom configuration" in:
    val seed = FACTORY_SEED
    val interval = SPAWN_INTERVAL_FACTORY
    val system = SpawnSystem.withConfig(interval, Some(seed))

    system.spawnInterval shouldBe interval