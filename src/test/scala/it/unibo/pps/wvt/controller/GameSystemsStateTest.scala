package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.ecs.components.{PositionComponent, TrollType, WizardType}
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.GamePlayConstants.INITIAL_ELIXIR
import it.unibo.pps.wvt.utilities.GridMapper
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameSystemsStateTest extends AnyFlatSpec with Matchers:
  import GameScenarioDSL.*

  "A GameSystemsState" should "initialize with correct default values" in:
    val (_, state) = scenario(_ => {})

    state.currentWave shouldBe WAVE_FIRST
    state.getCurrentElixir shouldBe INITIAL_ELIXIR
    state.selectedWizardType shouldBe None

  it should "correctly select and clear a wizard for placement" in:
    val (_, state) = scenario(_ => {})

    val stateWithSelection = state.selectWizard(WizardType.Fire)
    stateWithSelection.selectedWizardType shouldBe Some(WizardType.Fire)

    val stateAfterClear = stateWithSelection.clearWizardSelection
    stateAfterClear.selectedWizardType shouldBe None

  it should "spend elixir successfully when sufficient funds are available" in:
    val (_, state) = scenario(_.withElixir(INITIAL_ELIXIR))

    val maybeStateAfterSpend = state.spendElixir(ELIXIR_SPEND_AMOUNT)

    maybeStateAfterSpend shouldBe defined
    maybeStateAfterSpend.get.getCurrentElixir shouldBe (INITIAL_ELIXIR - ELIXIR_SPEND_AMOUNT)


  it should "fail to spend elixir when funds are insufficient" in:
    val (_, state) = scenario(_.withElixir(ELIXIR_LOW))

    val maybeStateAfterSpend = state.spendElixir(INITIAL_ELIXIR)

    maybeStateAfterSpend should not be defined

  it should "handle a victory by advancing to the next wave and resetting elixir" in:
    val (_, state) = scenario(_.atWave(WAVE_FIRST))

    val nextWaveState = state.handleVictory()

    nextWaveState.currentWave shouldBe WAVE_SECOND
    nextWaveState.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "reset the game to its initial state" in:
    val (_, state) = scenario(s => s.atWave(WAVE_MID).withElixir(ELIXIR_ZERO))

    val resetState = state.reset()

    resetState.currentWave shouldBe WAVE_FIRST
    resetState.getCurrentElixir shouldBe INITIAL_ELIXIR
    resetState.selectedWizardType shouldBe None

  "A GameSystemsState's condition checker" should "detect a lose condition when a troll reaches the end" in:
    val (world0, state) = scenario(_.withTroll(TrollType.Base).at(GRID_ROW_MID, 0))
    val trollEntity = world0.getEntitiesByType("troll").head
    val world = world0
      .updateComponent[PositionComponent](
        trollEntity,
        _.copy(position = world0
          .getComponent[PositionComponent](trollEntity)
          .get.position.copy(x = GridMapper.getCellBounds(0, 0)
            ._1))
      )

    val loseEvent = state.checkLoseCondition(world)

    loseEvent shouldBe Some(GameEvent.GameLost)

  it should "not detect a lose condition if no troll has reached the end" in:
    val (world, state) = scenario(_.withTroll(TrollType.Base).at(GRID_ROW_MID, GRID_COL_MID))

    val loseEvent = state.checkLoseCondition(world)

    loseEvent should not be defined

  it should "detect a win condition when all trolls are defeated and the wave is over" in:
    val (world, initialState) = scenario(_.withWizard(WizardType.Wind).at(GRID_ROW_MID, GRID_COL_START))

    // Simulate the state where the spawner has finished its job
    val state = initialState.copy(spawn = initialState.spawn.copy(hasSpawnedAtLeastOnce = true, isActive = false))

    val winEvent = state.checkWinCondition(world)

    winEvent shouldBe Some(GameEvent.GameWon)

  it should "not detect a win condition if trolls are still present" in:
    val (world, initialState) = scenario: s =>
      s.withWizard(WizardType.Wind).at(GRID_ROW_MID, GRID_COL_START)
      s.withTroll(TrollType.Base).at(GRID_ROW_MID, GRID_COL_END)

    val state = initialState.copy(spawn = initialState.spawn.copy(hasSpawnedAtLeastOnce = true, isActive = false))

    val winEvent = state.checkWinCondition(world)

    winEvent should not be defined

  it should "not detect a win condition if the spawn system is still active" in:
    val (world, initialState) = scenario(_.withWizard(WizardType.Wind).at(GRID_ROW_MID, GRID_COL_START))

    // Simulate the state where the spawner is still active
    val state = initialState.copy(spawn = initialState.spawn.copy(hasSpawnedAtLeastOnce = true, isActive = true))

    val winEvent = state.checkWinCondition(world)

    winEvent should not be defined