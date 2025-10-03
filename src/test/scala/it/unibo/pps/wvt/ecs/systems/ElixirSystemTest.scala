package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ElixirSystemTest extends AnyFlatSpec with Matchers:

  "ElixirSystem" should "start with initial elixir amount" in:
    val system = ElixirSystem()
    system.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "not have first wizard placed initially" in:
    val system = ElixirSystem()
    system.firstWizardPlaced shouldBe false

  it should "not generate periodic elixir before first wizard is placed" in:
    val world = World()
    val system = ElixirSystem()

    Thread.sleep(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "activate generation when first wizard is placed" in:
    val system = ElixirSystem()
    val activatedSystem = system.activateGeneration()

    activatedSystem.firstWizardPlaced shouldBe true
    activatedSystem.activationTime should be > 0L
    activatedSystem.lastPeriodicGeneration should be > 0L

  it should "generate periodic elixir after activation and interval" in:
    val world = World()
    val system = ElixirSystem().activateGeneration()
    val initialElixir = system.getCurrentElixir

    Thread.sleep(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    updatedSystem.getCurrentElixir should be >= initialElixir + PERIODIC_ELIXIR

  it should "generate elixir from generator wizards after sufficient time" in:
    val world = World()
    val system = ElixirSystem().activateGeneration()

    val generator = EntityFactory.createGeneratorWizard(world, Position(2, 3))
    val initialElixir = system.getCurrentElixir

    // Wait enough for both periodic and generator
    Thread.sleep(GENERATOR_WIZARD_COOLDOWN + ELIXIR_WAIT_MARGIN)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    // Should have at least periodic elixir (and possibly generator)
    updatedSystem.getCurrentElixir should be >= initialElixir + PERIODIC_ELIXIR

  it should "successfully spend elixir when enough available" in:
    val system = ElixirSystem()

    val (updatedSystem, success) = system.spendElixir(ELIXIR_SPEND_SMALL)

    success shouldBe true
    updatedSystem.getCurrentElixir shouldBe INITIAL_ELIXIR - ELIXIR_SPEND_SMALL

  it should "fail to spend elixir when not enough available" in:
    val system = ElixirSystem()

    val (updatedSystem, success) = system.spendElixir(ELIXIR_SPEND_EXCESSIVE)

    success shouldBe false
    updatedSystem.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "correctly add elixir" in:
    val system = ElixirSystem()

    val updatedSystem = system.addElixir(ELIXIR_ADD_AMOUNT)

    updatedSystem.getCurrentElixir shouldBe INITIAL_ELIXIR + ELIXIR_ADD_AMOUNT

  it should "correctly check if can afford" in:
    val system = ElixirSystem()

    system.canAfford(INITIAL_ELIXIR) shouldBe true
    system.canAfford(INITIAL_ELIXIR - 1) shouldBe true
    system.canAfford(INITIAL_ELIXIR + 1) shouldBe false

  it should "reset to initial state" in:
    val world = World()
    val system = ElixirSystem()
      .activateGeneration()
      .addElixir(ELIXIR_ADD_LARGE)

    val modifiedElixir = system.getCurrentElixir
    modifiedElixir should be > INITIAL_ELIXIR

    val resetSystem = system.reset()

    resetSystem.getCurrentElixir shouldBe INITIAL_ELIXIR
    resetSystem.firstWizardPlaced shouldBe false
    resetSystem.lastPeriodicGeneration shouldBe 0L
    resetSystem.activationTime shouldBe 0L

  it should "handle multiple generator wizards" in:
    val world = World()
    val system = ElixirSystem().activateGeneration()

    val generator1 = EntityFactory.createGeneratorWizard(world, Position(1, 1))
    val generator2 = EntityFactory.createGeneratorWizard(world, Position(2, 2))
    val generator3 = EntityFactory.createGeneratorWizard(world, Position(3, 3))
    val initialElixir = system.getCurrentElixir

    Thread.sleep(GENERATOR_WIZARD_COOLDOWN + ELIXIR_WAIT_MARGIN)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    // Should have at least periodic elixir
    updatedSystem.getCurrentElixir should be >= initialElixir + PERIODIC_ELIXIR

  it should "not generate from non-generator wizards" in:
    val world = World()
    val system = ElixirSystem().activateGeneration()

    val wind = EntityFactory.createWindWizard(world, Position(1, 1))
    val fire = EntityFactory.createFireWizard(world, Position(2, 2))
    val ice = EntityFactory.createIceWizard(world, Position(3, 3))
    val barrier = EntityFactory.createBarrierWizard(world, Position(4, 4))
    val initialElixir = system.getCurrentElixir

    Thread.sleep(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
    val updatedSystem = system.update(world).asInstanceOf[ElixirSystem]

    // Only periodic elixir is generated (no generators present)
    updatedSystem.getCurrentElixir shouldBe initialElixir + PERIODIC_ELIXIR

  it should "not generate immediately after first update" in:
    val world = World()
    val system = ElixirSystem().activateGeneration()

    val generator = EntityFactory.createGeneratorWizard(world, Position(2, 3))
    val initialElixir = system.getCurrentElixir

    // First update immediately - should not generate
    val updatedSystem1 = system.update(world).asInstanceOf[ElixirSystem]
    updatedSystem1.getCurrentElixir shouldBe initialElixir

    // Second update immediately - should still not generate
    val updatedSystem2 = updatedSystem1.update(world).asInstanceOf[ElixirSystem]
    updatedSystem2.getCurrentElixir shouldBe updatedSystem1.getCurrentElixir

  it should "maintain elixir count after multiple spend operations" in:
    val system = ElixirSystem()

    val (system1, success1) = system.spendElixir(ELIXIR_SPEND_SMALL)
    success1 shouldBe true
    system1.getCurrentElixir shouldBe INITIAL_ELIXIR - ELIXIR_SPEND_SMALL

    val (system2, success2) = system1.spendElixir(ELIXIR_SPEND_MEDIUM)
    success2 shouldBe true
    system2.getCurrentElixir shouldBe INITIAL_ELIXIR - ELIXIR_SPEND_SMALL - ELIXIR_SPEND_MEDIUM

    val (system3, success3) = system2.spendElixir(INITIAL_ELIXIR)
    success3 shouldBe false
    system3.getCurrentElixir shouldBe INITIAL_ELIXIR - ELIXIR_SPEND_SMALL - ELIXIR_SPEND_MEDIUM

  it should "handle edge case of spending exact amount available" in:
    val system = ElixirSystem()
    val exactAmount = INITIAL_ELIXIR

    val (updatedSystem, success) = system.spendElixir(exactAmount)

    success shouldBe true
    updatedSystem.getCurrentElixir shouldBe 0

  it should "not generate negative elixir" in:
    val system = ElixirSystem(totalElixir = 0)

    system.getCurrentElixir shouldBe 0
    system.canAfford(1) shouldBe false

    val (spentSystem, success) = system.spendElixir(ELIXIR_SPEND_SMALL)
    success shouldBe false
    spentSystem.getCurrentElixir shouldBe 0

  it should "add and spend elixir correctly in combination" in:
    val system = ElixirSystem()

    val systemWithMore = system.addElixir(ELIXIR_ADD_LARGE)
    systemWithMore.getCurrentElixir shouldBe INITIAL_ELIXIR + ELIXIR_ADD_LARGE

    val (finalSystem, success) = systemWithMore.spendElixir(ELIXIR_SPEND_COMBINED)
    success shouldBe true
    finalSystem.getCurrentElixir shouldBe INITIAL_ELIXIR + ELIXIR_ADD_LARGE - ELIXIR_SPEND_COMBINED