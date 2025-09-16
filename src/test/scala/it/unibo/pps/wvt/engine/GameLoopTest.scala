package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.input.*
import it.unibo.pps.wvt.model.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameLoopTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var engine: GameEngine = _
  var gameLoop: GameLoop = _

  override def beforeEach(): Unit = {
    engine = new TestGameEngine()
    gameLoop = new GameLoopImpl(engine)
  }

  override def afterEach(): Unit = {
    if (gameLoop.isRunning) {
      gameLoop.stop()
    }
  }

  "GameLoop" should "initialize in stopped state" in {
    gameLoop.isRunning shouldBe false
    gameLoop.getCurrentFps shouldBe 0
  }

  it should "start and stop correctly" in {
    gameLoop.isRunning shouldBe false

    gameLoop.start()
    gameLoop.isRunning shouldBe true

    Thread.sleep(100) // Let it run briefly

    gameLoop.stop()
    Thread.sleep(100) // Give time to stop
    gameLoop.isRunning shouldBe false
  }

  it should "not start multiple times" in {
    gameLoop.start()
    gameLoop.isRunning shouldBe true

    // Try to start again - should not create multiple threads
    gameLoop.start()
    gameLoop.isRunning shouldBe true

    gameLoop.stop()
  }

  it should "update the engine periodically" in {
    val testEngine = engine.asInstanceOf[TestGameEngine]
    testEngine.updateCount shouldBe 0

    gameLoop.start()
    Thread.sleep(200) // Run for 200ms
    gameLoop.stop()

    // Should have multiple updates in 200ms (at 60 FPS, expect ~12 updates)
    testEngine.updateCount should be > 5
  }

  it should "calculate FPS" in {
    gameLoop.start()

    // Wait for at least one second for FPS calculation
    Thread.sleep(1100)

    val fps = gameLoop.getCurrentFps
    gameLoop.stop()

    // Should be close to 60 FPS (allowing some variance)
    fps should be > 50
    fps should be < 70
  }

  it should "stop updating when engine stops" in {
    val testEngine = engine.asInstanceOf[TestGameEngine]

    gameLoop.start()
    Thread.sleep(100)

    val countBefore = testEngine.updateCount

    // Stop the engine (not the loop)
    testEngine.stop()
    Thread.sleep(100)

    val countAfter = testEngine.updateCount

    gameLoop.stop()

    // Updates should stop when engine stops
    countAfter shouldBe countBefore
  }

  "GameLoop factory" should "create loop with engine" in {
    val engine = GameEngine.create()
    val loop = GameLoop.create(engine)

    loop shouldBe a [GameLoopImpl]
    loop.isRunning shouldBe false

    engine.stop()
  }
}

// Helper class
class TestGameEngine extends GameEngine {
  private var _isRunning: Boolean = true
  private var _gameState: GameState = GameState.initial()
  var updateCount: Int = 0

  override def initialize(): Unit = {}

  override def start(): Unit = {
    _isRunning = true
  }

  override def stop(): Unit = {
    _isRunning = false
  }

  override def update(deltaTime: Long): Unit = {
    if (_isRunning) {
      updateCount += 1
      _gameState = _gameState.copy(elapsedTime = _gameState.elapsedTime + deltaTime)
    }
  }

  override def isRunning: Boolean = _isRunning
  override def currentState: GameState = _gameState

  override def processEvent(event: GameEvent): Unit =
    event match {
      case GameEvent.Stop => stop()
      case _ => // Ignore other events for this test
    }

  override def getInputSystem: Option[InputSystem] =
    Some(InputSystem())
}

class GameLoopInputSystemTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var engine: GameEngine = _
  var gameLoop: GameLoop = _

  override def beforeEach(): Unit = {
    engine = GameEngine.create()
  }

  override def afterEach(): Unit = {
    if (engine.isRunning) {
      engine.stop()
    }
  }

  "GameLoop" should "initialize with InputSystem" in {
    engine.start()

    val inputSystem = engine.getInputSystem
    inputSystem shouldBe defined
    inputSystem.get shouldBe a [InputSystem]

    engine.stop()
  }

  "InputSystem through GameLoop" should "handle mouse clicks correctly" in {
    engine.start()

    val inputSystem = engine.getInputSystem.get

    // Test valid click inside grid
    val validX = GRID_OFFSET_X.toInt + 50
    val validY = GRID_OFFSET_Y.toInt + 50
    val result = inputSystem.handleMouseClick(validX, validY)

    result.isValid shouldBe true
    result.position.isValid shouldBe true
    result.error shouldBe None

    // Test invalid click outside grid
    val invalidResult = inputSystem.handleMouseClick(10, 10)
    invalidResult.isValid shouldBe false
    invalidResult.error shouldBe defined

    engine.stop()
  }

  "InputSystem" should "be accessible during game loop execution" in {
    engine.start()

    // Let game loop run
    Thread.sleep(100)

    // Access input system while loop is running
    val inputSystem = engine.getInputSystem
    inputSystem shouldBe defined

    // Test position conversion
    val testPosition = Position(2, 3)
    val screenCoords = inputSystem.get.positionToScreen(testPosition)
    screenCoords shouldBe defined

    engine.stop()
  }

  "Grid click events" should "be processable through engine" in {
    engine.start()

    val testPosition = Position(2, 3)
    val clickEvent = GameEvent.GridClicked(testPosition, 100, 100)

    // Process the click event
    engine.processEvent(clickEvent)

    // Event should be processed without errors
    // (In Sprint 1, it just logs; Sprint 2 will add actual logic)
    Thread.sleep(50) // Give time for processing

    engine.stop()
  }

  "Key press events" should "be processable through engine" in {
    engine.start()

    val keyEvent = GameEvent.KeyPressed("ESCAPE")

    // Process the key event
    engine.processEvent(keyEvent)

    Thread.sleep(50)

    // For Sprint 1, just verify no crash
    // Sprint 2 will add actual key handling

    engine.stop()
  }

  "InputSystem integration" should "work with paused game" in {
    engine.processEvent(GameEvent.ShowGameView)
    engine.start()

    // Pause the game
    engine.processEvent(GameEvent.Pause)
    Thread.sleep(100)

    engine.currentState.isPaused shouldBe true

    // Input system should still be accessible when paused
    val inputSystem = engine.getInputSystem
    inputSystem shouldBe defined

    // Should still handle clicks when paused
    val validX = GRID_OFFSET_X.toInt + 50
    val validY = GRID_OFFSET_Y.toInt + 50
    val result = inputSystem.get.handleMouseClick(validX, validY)
    result.isValid shouldBe true

    engine.stop()
  }

  "InputSystem" should "remain consistent across multiple game loop cycles" in {
    engine.start()

    val inputSystem1 = engine.getInputSystem.get

    // Let loop run for multiple cycles
    Thread.sleep(200)

    val inputSystem2 = engine.getInputSystem.get

    // Should be the same instance
    inputSystem1 should be theSameInstanceAs inputSystem2

    // Test consistency of results
    val testX = GRID_OFFSET_X.toInt + 30
    val testY = GRID_OFFSET_Y.toInt + 30

    val result1 = inputSystem1.handleMouseClick(testX, testY)
    val result2 = inputSystem2.handleMouseClick(testX, testY)

    result1 shouldBe result2

    engine.stop()
  }

  "Complete input flow" should "work from click to event processing" in {
    engine.processEvent(GameEvent.ShowGameView)
    engine.start()

    // Simulate complete input flow
    val inputSystem = engine.getInputSystem.get

    // 1. Handle mouse click
    val clickX = GRID_OFFSET_X.toInt + 100
    val clickY = GRID_OFFSET_Y.toInt + 100
    val clickResult = inputSystem.handleMouseClick(clickX, clickY)

    clickResult.isValid shouldBe true
    val position = clickResult.position

    // 2. Create and process grid click event
    val gridClickEvent = GameEvent.GridClicked(position, clickX, clickY)
    engine.processEvent(gridClickEvent)

    Thread.sleep(100)

    // 3. Verify position mapping is consistent
    val screenCoords = inputSystem.positionToScreen(position)
    screenCoords shouldBe defined

    engine.stop()
  }

  "InputSystem validation" should "work correctly through game loop" in {
    engine.start()

    val inputSystem = engine.getInputSystem.get

    // Test position validation
    val validPos = Position(2, 3)
    inputSystem.isValidPosition(validPos) shouldBe true

    val invalidPos = Position(-1, -1, allowInvalid = true)
    inputSystem.isValidPosition(invalidPos) shouldBe false

    // Test edge positions
    val edgePos = Position(GRID_ROWS - 1, GRID_COLS - 1)
    inputSystem.isValidPosition(edgePos) shouldBe true

    engine.stop()
  }
}