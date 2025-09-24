package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ElixirSystemTest extends AnyFlatSpec with Matchers:

  "ElixirSystem" should "start with initial elixir amount" in:
    val system = ElixirSystem()
    system.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "spend elixir when enough is available" in:
    val system = ElixirSystem(totalElixir = 100)
    val (newSystem, success) = system.spendElixir(50)

    success shouldBe true
    newSystem.getCurrentElixir shouldBe 50

  it should "not spend elixir when insufficient" in:
    val system = ElixirSystem(totalElixir = 30)
    val (newSystem, success) = system.spendElixir(50)

    success shouldBe false
    newSystem.getCurrentElixir shouldBe 30

  it should "add elixir correctly" in:
    val system = ElixirSystem(totalElixir = 100)
    val newSystem = system.addElixir(50)

    newSystem.getCurrentElixir shouldBe 150

  it should "check affordability correctly" in:
    val system = ElixirSystem(totalElixir = 100)

    system.canAfford(50) shouldBe true
    system.canAfford(100) shouldBe true
    system.canAfford(101) shouldBe false

  it should "reset to initial state" in:
    val system = ElixirSystem(totalElixir = 500, lastPeriodicGeneration = 123456L)
    val resetSystem = system.reset()

    resetSystem.getCurrentElixir shouldBe INITIAL_ELIXIR
    resetSystem.lastPeriodicGeneration should not be 123456L

  it should "generate periodic elixir when interval has passed" in:
    val world = World()
    val oldTime = System.currentTimeMillis() - ELIXIR_GENERATION_INTERVAL - 1000
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = oldTime)

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe (100 + PERIODIC_ELIXIR)

  it should "not generate periodic elixir when interval has not passed" in:
    val world = World()
    val recentTime = System.currentTimeMillis() - 1000 // 1 second ago
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = recentTime)

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe 100

  it should "generate elixir from generator wizards" in:
    val world = World()
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = System.currentTimeMillis())

    // Create a generator wizard
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = 10, cooldown = 5))
    world.addComponent(wizard, CooldownComponent(0L)) // No cooldown, can generate immediately

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe 110

  it should "not generate elixir from generator wizards on cooldown" in:
    val world = World()
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = System.currentTimeMillis())
    val futureTime = System.currentTimeMillis() + 10000 // 10 seconds in future

    // Create a generator wizard on cooldown
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = 10, cooldown = 5))
    world.addComponent(wizard, CooldownComponent(futureTime))

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe 100

  it should "ignore non-generator wizards" in:
    val world = World()
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = System.currentTimeMillis())

    // Create a fire wizard (not generator)
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = 10, cooldown = 5))
    world.addComponent(wizard, CooldownComponent(0L))

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe 100

  it should "update cooldown after generation" in:
    val world = World()
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = System.currentTimeMillis())

    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = 10, cooldown = 5))
    world.addComponent(wizard, CooldownComponent(0L))

    system.update(world)

    val cooldown = world.getComponent[CooldownComponent](wizard)
    cooldown should be(defined)
    cooldown.get.remainingTime should be > System.currentTimeMillis()

  it should "handle multiple generator wizards" in:
    val world = World()
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = System.currentTimeMillis())

    // Create two generator wizards
    val wizard1 = world.createEntity()
    world.addComponent(wizard1, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard1, ElixirGeneratorComponent(elixirPerSecond = 10, cooldown = 5))
    world.addComponent(wizard1, CooldownComponent(0L))

    val wizard2 = world.createEntity()
    world.addComponent(wizard2, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard2, ElixirGeneratorComponent(elixirPerSecond = 15, cooldown = 3))
    world.addComponent(wizard2, CooldownComponent(0L))

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe 125 // 100 + 10 + 15

  it should "update correctly through full update cycle" in:
    val world = World()
    val oldTime = System.currentTimeMillis() - ELIXIR_GENERATION_INTERVAL - 1000
    val system = ElixirSystem(totalElixir = 100, lastPeriodicGeneration = oldTime)

    // Create a generator wizard
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = 5, cooldown = 10))
    world.addComponent(wizard, CooldownComponent(0L))

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    // Should have periodic + generator elixir
    updatedSystem.getCurrentElixir shouldBe (100 + PERIODIC_ELIXIR + 5)