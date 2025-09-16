package it.unibo.pps.wvt.engine

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameEngineTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var engine: GameEngine = _

  override def beforeEach(): Unit = {
    engine = GameEngine.create()
  }

  override def afterEach(): Unit = {
    if (engine.isRunning) {
      engine.stop()
    }
  }

  "GameEngine" should "initialize with correct default state" in {
    engine.isRunning shouldBe false
    engine.currentState.phase shouldBe GamePhase.MainMenu
    engine.currentState.isPaused shouldBe false
    engine.currentState.elapsedTime shouldBe 0L
  }

  it should "start and stop correctly" in {
    engine.isRunning shouldBe false

    engine.start()
    engine.isRunning shouldBe true

    engine.stop()
    engine.isRunning shouldBe false
  }

  it should "not start multiple times" in {
    engine.start()
    engine.isRunning shouldBe true

    engine.start()
    engine.isRunning shouldBe true // Should still be running, not crash
  }

  it should "update elapsed time when running" in {
    engine.start()

    val initialTime = engine.currentState.elapsedTime

    engine.update(100L)
    engine.currentState.elapsedTime shouldBe (initialTime + 100L)
  }

  it should "not update when not running" in {
    engine.isRunning shouldBe false

    val initialState = engine.currentState
    engine.update(100L)

    // State should not change when engine is not running
    engine.currentState shouldBe initialState
  }

  it should "handle multiple updates correctly" in {
    engine.start()

    engine.update(50L)
    engine.update(75L)
    engine.update(25L)

    engine.currentState.elapsedTime shouldBe 150L
  }

  "GameEngine factory" should "create independent instances" in {
    val engine1 = GameEngine.create()
    val engine2 = GameEngine.create()

    engine1 should not be theSameInstanceAs(engine2)

    engine1.start()
    engine1.isRunning shouldBe true
    engine2.isRunning shouldBe false

    engine1.stop()
  }
}

class GameEngineIntegrationTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var engine: GameEngine = _

  override def beforeEach(): Unit =
    engine = GameEngine.create()

  override def afterEach(): Unit =
    if (engine.isRunning)
      engine.stop()

  "GameEngine with EventSystem" should "process events correctly" in {
    engine.currentState.phase shouldBe GamePhase.MainMenu

    // Change to game view - processes immediately even without engine running
    engine.processEvent(GameEvent.ShowGameView)
    engine.currentState.phase shouldBe GamePhase.Playing

    // Start the engine
    engine.processEvent(GameEvent.Start)
    Thread.sleep(100) // Give time for thread to start
    engine.isRunning shouldBe true

    // Pause the game
    engine.processEvent(GameEvent.Pause)
    Thread.sleep(100) // Allow time for event processing in game loop
    engine.currentState.isPaused shouldBe true
    engine.currentState.phase shouldBe GamePhase.Paused

    // Resume the game
    engine.processEvent(GameEvent.Resume)
    Thread.sleep(100)
    engine.currentState.isPaused shouldBe false
    engine.currentState.phase shouldBe GamePhase.Playing
  }

  "GameEngine with GameLoop" should "update game state over time" in {
    engine.start()
    engine.isRunning shouldBe true

    val initialTime = engine.currentState.elapsedTime

    // Let the game run for a bit
    Thread.sleep(500)

    // Time should have progressed
    engine.currentState.elapsedTime should be > initialTime

    // FPS should be calculated after 1 second
    Thread.sleep(600) // Total > 1 second for FPS calculation
    engine.currentState.fps should be > 0

    engine.stop()
  }

  "GameEngine menu transitions" should "work correctly" in {
    // Start in main menu
    engine.currentState.phase shouldBe GamePhase.MainMenu

    // Go to info menu - works without engine running
    engine.processEvent(GameEvent.ShowInfoMenu)
    engine.currentState.phase shouldBe GamePhase.InfoMenu

    // Go to game view
    engine.processEvent(GameEvent.ShowGameView)
    engine.currentState.phase shouldBe GamePhase.Playing

    // Back to main menu
    engine.processEvent(GameEvent.ShowMainMenu)
    engine.currentState.phase shouldBe GamePhase.MainMenu
  }

  "GameEngine" should "handle pause during gameplay" in {
    // Change to game view first
    engine.processEvent(GameEvent.ShowGameView)
    engine.currentState.phase shouldBe GamePhase.Playing

    // Start the game
    engine.start()

    // Let it run
    Thread.sleep(200)
    val timeBeforePause = engine.currentState.elapsedTime
    timeBeforePause should be > 0L // Ensure time has passed

    // Pause
    engine.processEvent(GameEvent.Pause)
    Thread.sleep(100) // Give time to process pause
    engine.currentState.isPaused shouldBe true
    engine.currentState.phase shouldBe GamePhase.Paused

    // Wait a bit and check time hasn't progressed much
    Thread.sleep(200)
    val timeAfterPause = engine.currentState.elapsedTime
    val pauseTimeDiff = timeAfterPause - timeBeforePause
    pauseTimeDiff should be < 50L // Should be minimal or zero

    // Resume
    engine.processEvent(GameEvent.Resume)
    Thread.sleep(200)

    // Time should progress again after resume
    engine.currentState.elapsedTime should be > timeAfterPause
    engine.currentState.isPaused shouldBe false
    engine.currentState.phase shouldBe GamePhase.Playing

    engine.stop()
  }

  "GameEngine singleton" should "maintain single instance" in {
    val engine1 = GameEngine.getInstance
    engine1 shouldBe defined

    val engine2 = GameEngine.getInstance
    engine2 shouldBe defined

    engine1.get shouldBe theSameInstanceAs(engine2.get)
  }

  "Complete game flow" should "work from menu to game and back" in {
    // Simulate complete user flow
    engine.currentState.phase shouldBe GamePhase.MainMenu

    // User clicks start game - change phase first
    engine.processEvent(GameEvent.ShowGameView)
    engine.currentState.phase shouldBe GamePhase.Playing

    // Then start the engine
    engine.processEvent(GameEvent.Start)
    Thread.sleep(100)
    engine.isRunning shouldBe true

    // Game runs for a while
    Thread.sleep(1100) // Need > 1 second for FPS calculation
    engine.currentState.elapsedTime should be > 0L
    engine.currentState.fps should be > 0

    // User pauses
    engine.processEvent(GameEvent.Pause)
    Thread.sleep(100)
    engine.currentState.phase shouldBe GamePhase.Paused
    engine.currentState.isPaused shouldBe true

    // User goes back to menu
    engine.processEvent(GameEvent.ShowMainMenu)
    Thread.sleep(100)
    engine.currentState.phase shouldBe GamePhase.MainMenu

    // Cleanup
    engine.stop()
    engine.isRunning shouldBe false
  }

  "Engine Start event" should "be processed correctly" in {
    engine.isRunning shouldBe false

    // Process Start event - should start the engine
    engine.processEvent(GameEvent.Start)
    Thread.sleep(100) // Give time for thread to start

    engine.isRunning shouldBe true

    engine.stop()
  }
}