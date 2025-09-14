package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.engine.handlers.*
import it.unibo.pps.wvt.model.*
import it.unibo.pps.wvt.utilities.GameConstants
import it.unibo.pps.wvt.utilities.TestConstants._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameEngineTest extends AnyFlatSpec with Matchers {

  "GameState" should "initialize with correct default values" in {
    val state = GameState()
    state.phase shouldBe GamePhase.Menu
    state.wizards shouldBe empty
    state.trolls shouldBe empty
    state.projectiles shouldBe empty
    state.elixir shouldBe GameConstants.INITIAL_ELIXIR
    state.waveNumber shouldBe INITIAL_WAVE_NUM
  }

  it should "correctly determine wave completion" in {
    val state = GameState()
    state.isWaveCompleted shouldBe true

    val stateWithTrolls = state.copy(trolls = List(
      BaseTroll("t1", FIRST_POS, GameConstants.BASE_TROLL_HEALTH)
    ))
    stateWithTrolls.isWaveCompleted shouldBe false

    val stateWithSpawnQueue = state.copy(trollsToSpawn = List(
      BaseTroll("t2", SECOND_POS, GameConstants.BASE_TROLL_HEALTH)
    ))
    stateWithSpawnQueue.isWaveCompleted shouldBe false
  }

  it should "find and update entities correctly" in {
    val wiz = WindWizard("w1", SECOND_POS, GameConstants.WIND_WIZARD_HEALTH)
    val troll = BaseTroll("t1", FIRST_POS, GameConstants.BASE_TROLL_HEALTH)

    val state = GameState(
      wizards = List(wiz),
      trolls = List(troll)
    )

    state.getEntity("w1") shouldBe Some(wiz)
    state.getEntity("t1") shouldBe Some(troll)
    state.getEntity("unknown") shouldBe None

    val damagedWiz = wiz.takeDamage(DAMAGE)
    val updatedState = state.updateEntity(damagedWiz)
    updatedState.wizards.head.health shouldBe (GameConstants.WIND_WIZARD_HEALTH - DAMAGE)
  }
}

class EventHandlersTest extends AnyFlatSpec with Matchers {

  "SpawnHandler" should "spawn trolls only during InGame phase" in {
    val handler = new SpawnHandler()
    val troll = BaseTroll("t1", FIRST_POS, GameConstants.BASE_TROLL_HEALTH)
    val event = GameEvent.SpawnTroll(troll)

    // Should not spawn in Menu phase
    val menuState = GameState(phase = GamePhase.Menu)
    val (newMenuState, _) = handler.handle(event, menuState)
    newMenuState.trolls shouldBe empty

    // Should spawn in InGame phase
    val gameState = GameState(phase = GamePhase.InGame)
    val (newGameState, _) = handler.handle(event, gameState)
    newGameState.trolls should contain(troll)
    newGameState.grid.get(troll.position) shouldBe CellType.Troll
  }

  "PlacementHandler" should "place wizards with sufficient elixir" in {
    val handler = new PlacementHandler()
    val wizard = WindWizard("w1", SECOND_POS, GameConstants.WIND_WIZARD_HEALTH)
    val event = GameEvent.PlaceWizard(wizard, SECOND_POS)

    // With sufficient elixir
    val richState = GameState(phase = GamePhase.InGame, elixir = GameConstants.INITIAL_ELIXIR)
    val (newRichState, _) = handler.handle(event, richState)
    newRichState.wizards should have size 1
    newRichState.elixir shouldBe (GameConstants.INITIAL_ELIXIR - wizard.cost)
    newRichState.grid.get(SECOND_POS) shouldBe CellType.Wizard

    // Without sufficient elixir
    val poorState = GameState(phase = GamePhase.InGame, elixir = POOR_ELIXIR)
    val (newPoorState, _) = handler.handle(event, poorState)
    newPoorState.wizards shouldBe empty
    newPoorState.elixir shouldBe POOR_ELIXIR
  }

  it should "not place wizards on occupied cells" in {
    val handler = new PlacementHandler()
    val pos = SECOND_POS
    val wizard1 = WindWizard("w1", pos, GameConstants.WIND_WIZARD_HEALTH)
    val wizard2 = BarrierWizard("w2", pos, GameConstants.BARRIER_WIZARD_HEALTH)

    val state = GameState(
      phase = GamePhase.InGame,
      elixir = GameConstants.INITIAL_ELIXIR*2,
      wizards = List(wizard1),
      grid = Grid().set(pos, CellType.Wizard)
    )

    val event = GameEvent.PlaceWizard(wizard2, pos)
    val (newState, _) = handler.handle(event, state)

    newState.wizards should have size 1
    newState.wizards.head.id shouldBe "w1"
  }

  "CombatHandler" should "handle entity damage and destruction" in {
    val handler = new CombatHandler()
    val wizard = WindWizard("w1", SECOND_POS, GameConstants.WIND_WIZARD_HEALTH)
    val state = GameState(
      phase = GamePhase.InGame,
      wizards = List(wizard)
    )

    // Damage but not destroy
    val damageEvent = GameEvent.EntityDamaged("w1", DAMAGE)
    val (damagedState, events1) = handler.handle(damageEvent, state)
    damagedState.wizards.head.health shouldBe (GameConstants.WIND_WIZARD_HEALTH - DAMAGE)
    events1 shouldBe empty

    // Damage and destroy
    val killEvent = GameEvent.EntityDamaged("w1", DAMAGE_AND_DESTROY)
    val (killedState, events2) = handler.handle(killEvent, damagedState)
    killedState.wizards shouldBe empty
    events2 should contain(GameEvent.EntityDestroyed("w1"))
  }

  it should "reward elixir for destroying trolls" in {
    val handler = new CombatHandler()
    val troll = BaseTroll("t1", FIRST_POS, GameConstants.BASE_TROLL_HEALTH)
    val state = GameState(
      phase = GamePhase.InGame,
      trolls = List(troll),
      elixir = GameConstants.INITIAL_ELIXIR
    )

    val damageEvent = GameEvent.EntityDamaged("t1", DAMAGE_AND_DESTROY)
    val (damagedState, events) = handler.handle(damageEvent, state)

    damagedState.trolls shouldBe empty
    events should contain(GameEvent.EntityDestroyed("t1"))

    val (finalState, _) = handler.handle(events.head, damagedState)
    finalState.elixir shouldBe (GameConstants.INITIAL_ELIXIR + GameConstants.BASE_TROLL_REWARD)
  }

  "PhaseHandler" should "handle game phase transitions" in {
    val handler = new PhaseHandler()

    // Pause during game
    val gameState = GameState(phase = GamePhase.InGame)
    val (pausedState, _) = handler.handle(GameEvent.PauseGame, gameState)
    pausedState.phase shouldBe GamePhase.Paused

    // Resume from pause
    val (resumedState, _) = handler.handle(GameEvent.ResumeGame, pausedState)
    resumedState.phase shouldBe GamePhase.InGame

    // Start wave from menu
    val menuState = GameState(phase = GamePhase.Menu)
    val (startedState, _) = handler.handle(GameEvent.StartWave, menuState)
    startedState.phase shouldBe GamePhase.InGame
    startedState.waveNumber shouldBe 1
    startedState.trollsToSpawn should not be empty

    // Wave completion
    val (completedState, _) = handler.handle(GameEvent.WaveCompleted(1), startedState)
    completedState.phase shouldBe GamePhase.WaveComplete

    // Next wave
    val (nextWaveState, _) = handler.handle(GameEvent.StartWave, completedState)
    nextWaveState.phase shouldBe GamePhase.InGame
    nextWaveState.waveNumber shouldBe INITIAL_WAVE_NUM + 1
  }

  "ElixirHandler" should "generate elixir correctly" in {
    val handler = new ElixirHandler()
    val state = GameState(elixir = GameConstants.INITIAL_ELIXIR)

    val event = GameEvent.ElixirGenerated(GameConstants.PERIODIC_ELIXIR)
    val (newState, _) = handler.handle(event, state)

    newState.elixir shouldBe (GameConstants.INITIAL_ELIXIR + GameConstants.PERIODIC_ELIXIR)
  }
}

class GameEngineImplSpec extends AnyFlatSpec with Matchers {

  def createEngine(): GameEngineImpl = new GameEngineImpl()

  "GameEngine" should "process events in queue" in {
    val engine = createEngine()

    engine.processEvent(GameEvent.StartWave)
    engine.update(DELTA)

    engine.currentState.phase shouldBe GamePhase.InGame
    engine.currentState.trollsToSpawn should not be empty
  }

  it should "handle wizard placement" in {
    val engine = createEngine()

    // Start the game
    engine.processEvent(GameEvent.StartWave)
    engine.update(DELTA)

    // Place a wizard
    val wizard = WindWizard("w1", POS, GameConstants.WIND_WIZARD_HEALTH)
    engine.processEvent(GameEvent.PlaceWizard(wizard, POS))
    engine.update(DELTA)

    engine.currentState.wizards should have size 1
    engine.currentState.elixir shouldBe (GameConstants.INITIAL_ELIXIR - GameConstants.WIND_WIZARD_COST) // Started with 200, spent 100
  }

  it should "handle pause and resume" in {
    val engine = createEngine()

    engine.processEvent(GameEvent.StartWave)
    engine.update(DELTA)
    engine.currentState.phase shouldBe GamePhase.InGame

    engine.processEvent(GameEvent.PauseGame)
    engine.update(DELTA)
    engine.currentState.phase shouldBe GamePhase.Paused

    engine.processEvent(GameEvent.ResumeGame)
    engine.update(DELTA)
    engine.currentState.phase shouldBe GamePhase.InGame
  }

  it should "use extension methods for fluent API" in {
    import GameEngine._

    val engine = createEngine()

    engine
      .startGame()
      .pauseGame()
      .resumeGame()

    engine.update(DELTA)
    engine.currentState.phase shouldBe GamePhase.InGame
  }
}
