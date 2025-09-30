package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ElixirSystemTest extends AnyFlatSpec with Matchers:

  "ElixirSystem" should "start with initial elixir amount" in:
    val system = ElixirSystem()
    system.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "start with generation disabled" in:
    val system = ElixirSystem()
    system.firstWizardPlaced shouldBe false

  it should "spend elixir when enough is available" in:
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT)
    val (newSystem, success) = system.spendElixir(TEST_ELIXIR_SPEND)
    success shouldBe true
    newSystem.getCurrentElixir shouldBe TEST_ELIXIR_REMAINING

  it should "not spend elixir when insufficient" in:
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_INSUFFICIENT)
    val (newSystem, success) = system.spendElixir(TEST_ELIXIR_SPEND)
    success shouldBe false
    newSystem.getCurrentElixir shouldBe TEST_ELIXIR_INSUFFICIENT

  it should "add elixir correctly" in:
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT)
    val newSystem = system.addElixir(TEST_ELIXIR_SPEND)
    newSystem.getCurrentElixir shouldBe TEST_ELIXIR_AFTER_ADD

  it should "check affordability correctly" in:
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT)
    system.canAfford(TEST_ELIXIR_SPEND) shouldBe true
    system.canAfford(TEST_ELIXIR_AMOUNT) shouldBe true
    system.canAfford(TEST_ELIXIR_TOO_MUCH) shouldBe false

  it should "activate generation when activateGeneration is called" in:
    val system = ElixirSystem()
    val activatedSystem = system.activateGeneration()
    activatedSystem.firstWizardPlaced shouldBe true
    activatedSystem.lastPeriodicGeneration shouldBe TEST_TIMER_ZERO

  it should "reset to initial state" in:
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_HIGH, lastPeriodicGeneration = TEST_OLD_TIMESTAMP, firstWizardPlaced = true)
    val resetSystem = system.reset()
    resetSystem.getCurrentElixir shouldBe INITIAL_ELIXIR
    resetSystem.firstWizardPlaced shouldBe false
    resetSystem.lastPeriodicGeneration shouldBe TEST_TIMER_ZERO

  it should "not generate periodic elixir before first wizard is placed" in:
    val world = World()
    val oldTime = System.currentTimeMillis() - ELIXIR_GENERATION_INTERVAL - TEST_TIME_BUFFER
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = oldTime, firstWizardPlaced = false)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT

  it should "initialize timer on first update after activation without generating elixir" in:
    val world = World()
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = TEST_TIMER_ZERO, firstWizardPlaced = true)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT
    updatedSystem.lastPeriodicGeneration should be > TEST_TIMER_ZERO

  it should "generate periodic elixir after first wizard and interval has passed" in:
    val world = World()
    val oldTime = System.currentTimeMillis() - ELIXIR_GENERATION_INTERVAL - TEST_TIME_BUFFER
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = oldTime, firstWizardPlaced = true)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe (TEST_ELIXIR_AMOUNT + PERIODIC_ELIXIR)

  it should "not generate periodic elixir when interval has not passed" in:
    val world = World()
    val recentTime = System.currentTimeMillis() - TEST_TIME_SHORT
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = recentTime, firstWizardPlaced = true)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT

  it should "not generate elixir from generator wizards before first wizard is placed" in:
    val world = World()
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = TEST_TIMER_ZERO, firstWizardPlaced = false)
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_LOW, cooldown = TEST_GENERATOR_COOLDOWN_LONG))
    world.addComponent(wizard, CooldownComponent(TEST_TIMER_ZERO))

    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT

  it should "initialize generator cooldown without generating elixir on first cycle" in:
    val world = World()
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = System.currentTimeMillis(), firstWizardPlaced = true)
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_LOW, cooldown = TEST_GENERATOR_COOLDOWN_LONG))
    world.addComponent(wizard, CooldownComponent(TEST_TIMER_ZERO))
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT
    val cooldown = world.getComponent[CooldownComponent](wizard)
    cooldown should be(defined)
    cooldown.get.remainingTime should be > System.currentTimeMillis()

  it should "generate elixir from generator wizards after cooldown expires" in:
    val world = World()
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = System.currentTimeMillis(), firstWizardPlaced = true)
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_LOW, cooldown = TEST_GENERATOR_COOLDOWN_LONG))
    world.addComponent(wizard, CooldownComponent(System.currentTimeMillis() - TEST_TIME_SHORT))
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT + TEST_GENERATOR_ELIXIR_LOW

  it should "not generate elixir from generator wizards on cooldown" in:
    val world = World()
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = System.currentTimeMillis(), firstWizardPlaced = true)
    val futureTime = System.currentTimeMillis() + TEST_TIME_LONG
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_LOW, cooldown = TEST_GENERATOR_COOLDOWN_LONG))
    world.addComponent(wizard, CooldownComponent(futureTime))
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT

  it should "ignore non-generator wizards" in:
    val world = World()
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = System.currentTimeMillis(), firstWizardPlaced = true)
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_LOW, cooldown = TEST_GENERATOR_COOLDOWN_LONG))
    world.addComponent(wizard, CooldownComponent(TEST_TIMER_ZERO))
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_AMOUNT

  it should "handle multiple generator wizards" in:
    val world = World()
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = System.currentTimeMillis(), firstWizardPlaced = true)
    val wizard1 = world.createEntity()
    world.addComponent(wizard1, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard1, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_LOW, cooldown = TEST_GENERATOR_COOLDOWN_LONG))
    world.addComponent(wizard1, CooldownComponent(System.currentTimeMillis() - TEST_TIME_SHORT))
    val wizard2 = world.createEntity()
    world.addComponent(wizard2, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard2, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_HIGH, cooldown = TEST_GENERATOR_COOLDOWN_SHORT))
    world.addComponent(wizard2, CooldownComponent(System.currentTimeMillis() - TEST_TIME_SHORT))
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_MULTIPLE_GENERATORS

  it should "update correctly through full update cycle after activation" in:
    val world = World()
    val oldTime = System.currentTimeMillis() - ELIXIR_GENERATION_INTERVAL - TEST_TIME_BUFFER
    val system = ElixirSystem(totalElixir = TEST_ELIXIR_AMOUNT, lastPeriodicGeneration = oldTime, firstWizardPlaced = true)
    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Generator))
    world.addComponent(wizard, ElixirGeneratorComponent(elixirPerSecond = TEST_GENERATOR_ELIXIR_TINY, cooldown = TEST_GENERATOR_COOLDOWN_VERY_LONG))
    world.addComponent(wizard, CooldownComponent(System.currentTimeMillis() - TEST_TIME_SHORT))
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem.getCurrentElixir shouldBe TEST_ELIXIR_FULL_CYCLE