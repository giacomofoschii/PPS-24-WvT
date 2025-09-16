package it.unibo.pps.wvt.engine

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
}