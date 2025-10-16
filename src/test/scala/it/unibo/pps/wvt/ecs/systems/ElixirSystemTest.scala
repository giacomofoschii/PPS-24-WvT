package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ElixirSystemTest extends AnyFlatSpec with Matchers:

  behavior of "ElixirSystem"

  it should "start with the initial elixir amount" in:
    anElixirSystem
      .shouldHaveElixir(INITIAL_ELIXIR)
      .andShouldNotHaveFirstWizardPlaced

  it should "not generate periodic elixir before activation" in:
    givenAnElixirSystem
      .withWorld
      .afterWaiting(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .shouldHaveElixir(INITIAL_ELIXIR)

  it should "activate generation correctly when first wizard is placed" in:
    anElixirSystem
      .whenActivated
      .shouldHaveFirstWizardPlaced
      .andShouldHaveActivationTimeSet
      .andShouldHaveLastPeriodicGenerationSet

  it should "generate periodic elixir after activation and interval" in:
    givenAnElixirSystem
      .activated
      .withWorld
      .rememberingInitialElixir
      .afterWaiting(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .shouldHaveAtLeast(PERIODIC_ELIXIR).moreElixirThanInitial

  it should "generate elixir from generator wizards after cooldown" in:
    givenAnElixirSystem
      .activated
      .withWorld
      .andGeneratorWizardAt(Position(2, 3))
      .rememberingInitialElixir
      .afterWaiting(GENERATOR_WIZARD_COOLDOWN + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .shouldHaveAtLeast(PERIODIC_ELIXIR).moreElixirThanInitial

  it should "spend elixir correctly when enough available" in:
    anElixirSystem
      .whenSpending(ELIXIR_SPEND_SMALL)
      .shouldSucceed
      .andShouldHaveElixir(INITIAL_ELIXIR - ELIXIR_SPEND_SMALL)

  it should "fail to spend elixir when not enough available" in:
    anElixirSystem
      .whenSpending(ELIXIR_SPEND_EXCESSIVE)
      .shouldFail
      .andShouldHaveElixir(INITIAL_ELIXIR)

  it should "add elixir correctly" in:
    anElixirSystem
      .whenAdding(ELIXIR_ADD_AMOUNT)
      .shouldHaveElixir(INITIAL_ELIXIR + ELIXIR_ADD_AMOUNT)

  it should "correctly check if it can afford a cost" in:
    anElixirSystem
      .shouldBeAbleToAfford(INITIAL_ELIXIR)
      .andShouldBeAbleToAfford(INITIAL_ELIXIR - 1)
      .andShouldNotBeAbleToAfford(INITIAL_ELIXIR + 1)

  it should "reset to initial state correctly" in:
    anElixirSystem
      .activated
      .withAddedElixir(ELIXIR_ADD_LARGE)
      .shouldHaveMoreThan(INITIAL_ELIXIR)
      .whenReset
      .shouldHaveElixir(INITIAL_ELIXIR)
      .andShouldNotHaveFirstWizardPlaced
      .andShouldHaveZeroLastPeriodicGeneration
      .andShouldHaveZeroActivationTime

  it should "handle multiple generator wizards" in:
    givenAnElixirSystem
      .activated
      .withWorld
      .andGeneratorWizardAt(Position(1, 1))
      .andGeneratorWizardAt(Position(2, 2))
      .andGeneratorWizardAt(Position(3, 3))
      .rememberingInitialElixir
      .afterWaiting(GENERATOR_WIZARD_COOLDOWN + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .shouldHaveAtLeast(PERIODIC_ELIXIR).moreElixirThanInitial

  it should "not generate from non-generator wizards" in:
    givenAnElixirSystem
      .activated
      .withWorld
      .andWindWizardAt(Position(1, 1))
      .andFireWizardAt(Position(2, 2))
      .andIceWizardAt(Position(3, 3))
      .andBarrierWizardAt(Position(4, 4))
      .rememberingInitialElixir
      .afterWaiting(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .shouldHaveExactly(PERIODIC_ELIXIR).moreElixirThanInitial

  it should "not generate immediately after first update" in:
    givenAnElixirSystem
      .activated
      .withWorld
      .andGeneratorWizardAt(Position(2, 3))
      .rememberingInitialElixir
      .whenUpdatedImmediately
      .shouldHaveElixirEqualToInitial
      .whenUpdatedAgainImmediately
      .shouldHaveElixirEqualToPrevious

  it should "maintain correct elixir count after multiple spend operations" in:
    anElixirSystem
      .whenSpending(ELIXIR_SPEND_SMALL)
      .shouldSucceed.andShouldHaveElixir(INITIAL_ELIXIR - ELIXIR_SPEND_SMALL)
      .whenSpending(ELIXIR_SPEND_MEDIUM)
      .shouldSucceed.andShouldHaveElixir(INITIAL_ELIXIR - ELIXIR_SPEND_SMALL - ELIXIR_SPEND_MEDIUM)
      .whenSpending(INITIAL_ELIXIR)
      .shouldFail.andShouldHaveElixir(INITIAL_ELIXIR - ELIXIR_SPEND_SMALL - ELIXIR_SPEND_MEDIUM)

  it should "handle spending the exact amount available" in:
    anElixirSystem
      .whenSpending(INITIAL_ELIXIR)
      .shouldSucceed
      .andShouldHaveElixir(0)

  it should "not allow negative elixir values" in:
    anElixirSystemWith(0).elixir
      .shouldHaveElixir(0)
      .shouldNotBeAbleToAfford(1)
      .whenSpending(ELIXIR_SPEND_SMALL)
      .shouldFail
      .andShouldHaveElixir(0)

  it should "add and spend elixir correctly in combination" in:
    anElixirSystem
      .whenAdding(ELIXIR_ADD_LARGE)
      .shouldHaveElixir(INITIAL_ELIXIR + ELIXIR_ADD_LARGE)
      .whenSpending(ELIXIR_SPEND_COMBINED)
      .shouldSucceed
      .andShouldHaveElixir(INITIAL_ELIXIR + ELIXIR_ADD_LARGE - ELIXIR_SPEND_COMBINED)

  it should "cap elixir at 1000 when adding or generating" in:
    anElixirSystemWith(950).elixir
      .whenAdding(100)
      .shouldHaveElixir(1000)
      .whenAdding(2000)
      .shouldHaveElixir(1000)

  it should "not exceed 1000 elixir via periodic generation" in:
    anElixirSystemWith(950).elixir
      .activated
      .withWorld
      .afterWaiting(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .shouldHaveElixir(1000)

  it should "not exceed 1000 elixir via generator wizards" in:
    anElixirSystemWith(990).elixir
      .activated
      .withWorld
      .andGeneratorWizardAt(Position(1, 1))
      .afterWaiting(GENERATOR_WIZARD_COOLDOWN + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .shouldHaveElixir(1000)

  it should "update generation time even when at max elixir" in:
    anElixirSystemWith(1000).elixir
      .activated
      .withWorld
      .afterWaiting(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
      .rememberingLastPeriodicGeneration
      .whenUpdated
      .shouldHaveElixir(1000)
      .andLastPeriodicGenerationShouldBeUpdated

  it should "set correct activation time when activated" in:
    val before = System.currentTimeMillis()
    Thread.sleep(10)
    anElixirSystem
      .whenActivated
      .shouldHaveActivationTimeAfter(before)
      .andActivationTimeShouldEqualLastPeriodicGeneration

  it should "maintain same activation time across updates" in:
    givenAnElixirSystem
      .activated
      .withWorld
      .rememberingActivationTime
      .afterWaiting(ELIXIR_GENERATION_INTERVAL + ELIXIR_WAIT_MARGIN)
      .whenUpdated
      .activationTimeShouldNotChange

  private def anElixirSystem: ElixirSystemDSL =
    ElixirSystemDSL(ElixirSystem())

  private def givenAnElixirSystem: ElixirSystemDSL =
    anElixirSystem

  private def anElixirSystemWith(amount: Int): ElixirAmountBuilder =
    ElixirAmountBuilder(amount)

  case class ElixirAmountBuilder(amount: Int):
    def elixir: ElixirSystemDSL =
      ElixirSystemDSL(ElixirSystem(totalElixir = amount))

  case class ElixirSystemDSL(
      system: ElixirSystem,
      world: Option[World] = None,
      initialElixir: Option[Int] = None,
      previousElixir: Option[Int] = None,
      spendResult: Option[(ElixirSystem, Boolean)] = None,
      savedLastPeriodicGeneration: Option[Long] = None,
      savedActivationTime: Option[Long] = None
  ):

    def withWorld: ElixirSystemDSL = copy(world = Some(World()))

    def activated: ElixirSystemDSL =
      copy(system = system.activateGeneration())

    def withAddedElixir(amount: Int): ElixirSystemDSL =
      copy(system = system.addElixir(amount))

    def andGeneratorWizardAt(pos: Position): ElixirSystemDSL =
      world.foreach(EntityFactory.createGeneratorWizard(_, pos))
      this

    def andWindWizardAt(pos: Position): ElixirSystemDSL =
      world.foreach(EntityFactory.createWindWizard(_, pos))
      this

    def andFireWizardAt(pos: Position): ElixirSystemDSL =
      world.foreach(EntityFactory.createFireWizard(_, pos))
      this

    def andIceWizardAt(pos: Position): ElixirSystemDSL =
      world.foreach(EntityFactory.createIceWizard(_, pos))
      this

    def andBarrierWizardAt(pos: Position): ElixirSystemDSL =
      world.foreach(EntityFactory.createBarrierWizard(_, pos))
      this

    def rememberingInitialElixir: ElixirSystemDSL =
      copy(initialElixir = Some(system.getCurrentElixir))

    def rememberingLastPeriodicGeneration: ElixirSystemDSL =
      copy(savedLastPeriodicGeneration = Some(system.lastPeriodicGeneration))

    def rememberingActivationTime: ElixirSystemDSL =
      copy(savedActivationTime = Some(system.activationTime))

    def afterWaiting(millis: Long): ElixirSystemDSL =
      Thread.sleep(millis)
      this

    def whenUpdated: ElixirSystemDSL =
      world.map(w => copy(system = system.update(w)._2.asInstanceOf[ElixirSystem])).getOrElse(this)

    def whenUpdatedImmediately: ElixirSystemDSL =
      copy(
        system = world.map(w => system.update(w)._2.asInstanceOf[ElixirSystem]).getOrElse(system),
        previousElixir = Some(system.getCurrentElixir)
      )

    def whenUpdatedAgainImmediately: ElixirSystemDSL =
      copy(
        system = world.map(w => system.update(w)._2.asInstanceOf[ElixirSystem]).getOrElse(system),
        previousElixir = Some(system.getCurrentElixir)
      )

    def whenActivated: ElixirSystemDSL =
      copy(system = system.activateGeneration())

    def whenAdding(amount: Int): ElixirSystemDSL =
      copy(system = system.addElixir(amount))

    def whenSpending(amount: Int): ElixirSystemDSL =
      val result = system.spendElixir(amount)
      copy(system = result._1, spendResult = Some(result))

    def whenReset: ElixirSystemDSL =
      copy(system = ElixirSystem())

    def shouldHaveElixir(expected: Int): ElixirSystemDSL =
      system.getCurrentElixir shouldBe expected
      this

    def shouldHaveMoreThan(amount: Int): ElixirSystemDSL =
      system.getCurrentElixir should be > amount
      this

    def shouldHaveAtLeast(amount: Int): ElixirAmountComparison =
      ElixirAmountComparison(this, amount, ComparisonType.AtLeast)

    def shouldHaveExactly(amount: Int): ElixirAmountComparison =
      ElixirAmountComparison(this, amount, ComparisonType.Exactly)

    def shouldNotHaveFirstWizardPlaced: ElixirSystemDSL =
      system.firstWizardPlaced shouldBe false
      this

    def shouldHaveFirstWizardPlaced: ElixirSystemDSL =
      system.firstWizardPlaced shouldBe true
      this

    def andShouldHaveActivationTimeSet: ElixirSystemDSL =
      system.activationTime should be > 0L
      this

    def andShouldHaveLastPeriodicGenerationSet: ElixirSystemDSL =
      system.lastPeriodicGeneration should be > 0L
      this

    def shouldSucceed: ElixirSystemDSL =
      spendResult.foreach(_._2 shouldBe true)
      this

    def shouldFail: ElixirSystemDSL =
      spendResult.foreach(_._2 shouldBe false)
      this

    def andShouldHaveElixir(expected: Int): ElixirSystemDSL =
      shouldHaveElixir(expected)

    def andShouldNotHaveFirstWizardPlaced: ElixirSystemDSL =
      shouldNotHaveFirstWizardPlaced

    def andShouldHaveZeroLastPeriodicGeneration: ElixirSystemDSL =
      system.lastPeriodicGeneration shouldBe 0L
      this

    def andShouldHaveZeroActivationTime: ElixirSystemDSL =
      system.activationTime shouldBe 0L
      this

    def shouldBeAbleToAfford(amount: Int): ElixirSystemDSL =
      system.canAfford(amount) shouldBe true
      this

    def andShouldBeAbleToAfford(amount: Int): ElixirSystemDSL =
      shouldBeAbleToAfford(amount)

    def shouldNotBeAbleToAfford(amount: Int): ElixirSystemDSL =
      system.canAfford(amount) shouldBe false
      this

    def andShouldNotBeAbleToAfford(amount: Int): ElixirSystemDSL =
      shouldNotBeAbleToAfford(amount)

    def shouldHaveElixirEqualToInitial: ElixirSystemDSL =
      initialElixir.foreach(i => system.getCurrentElixir shouldBe i)
      this

    def shouldHaveElixirEqualToPrevious: ElixirSystemDSL =
      previousElixir.foreach(p => system.getCurrentElixir shouldBe p)
      this

    def andLastPeriodicGenerationShouldBeUpdated: ElixirSystemDSL =
      savedLastPeriodicGeneration.foreach(prev => system.lastPeriodicGeneration should be > prev)
      this

    def shouldHaveActivationTimeAfter(time: Long): ElixirSystemDSL =
      system.activationTime should be > time
      this

    def andActivationTimeShouldEqualLastPeriodicGeneration: ElixirSystemDSL =
      system.activationTime shouldBe system.lastPeriodicGeneration
      this

    def activationTimeShouldNotChange: ElixirSystemDSL =
      savedActivationTime.foreach(prev => system.activationTime shouldBe prev)
      this

  enum ComparisonType:
    case AtLeast, Exactly

  case class ElixirAmountComparison(dsl: ElixirSystemDSL, amount: Int, comparisonType: ComparisonType):
    def moreElixirThanInitial: ElixirSystemDSL =
      dsl.initialElixir.foreach: initial =>
        val diff = dsl.system.getCurrentElixir - initial
        comparisonType match
          case ComparisonType.AtLeast => diff should be >= amount
          case ComparisonType.Exactly => diff shouldBe amount
      dsl
