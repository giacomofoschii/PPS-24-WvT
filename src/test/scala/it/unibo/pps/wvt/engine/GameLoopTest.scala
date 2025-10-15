package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.controller.GameController
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameLoopTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  private var engine: TestGameEngine = _
  private var gameLoop: GameLoop     = _

  override def beforeEach(): Unit =
    engine = new TestGameEngine()
    gameLoop = new GameLoopImpl(engine)

  override def afterEach(): Unit =
    if gameLoop.isRunning then
      gameLoop.stop()
    Thread.sleep(MEDIUM_DELAY)

  "GameLoop" should "initialize in stopped state" in:
    gameLoop.isRunning shouldBe false
    gameLoop.getCurrentFps shouldBe INITIAL_FPS

  it should "start and stop correctly" in:
    gameLoop.isRunning shouldBe false

    gameLoop.start()
    gameLoop.isRunning shouldBe true

    Thread.sleep(MEDIUM_DELAY)

    gameLoop.stop()
    Thread.sleep(MEDIUM_DELAY)
    gameLoop.isRunning shouldBe false

  it should "be idempotent on multiple starts" in:
    gameLoop.start()
    gameLoop.isRunning shouldBe true

    gameLoop.start()
    gameLoop.isRunning shouldBe true

    gameLoop.stop()

  it should "be idempotent on multiple stops" in:
    gameLoop.start()
    gameLoop.stop()
    Thread.sleep(SHORT_DELAY)
    gameLoop.isRunning shouldBe false

    gameLoop.stop()
    gameLoop.isRunning shouldBe false

  it should "update the engine periodically" in:
    engine.updateCount shouldBe INITIAL_ENTITY_COUNT

    gameLoop.start()
    Thread.sleep(LONG_DELAY)
    gameLoop.stop()

    engine.updateCount should be >= MIN_UPDATES_200MS
    engine.updateCount should be <= MAX_UPDATES_200MS

  it should "calculate FPS over time" in:
    gameLoop.start()
    Thread.sleep(FPS_CALCULATION_DELAY)
    val fps = gameLoop.getCurrentFps
    gameLoop.stop()

    fps should be >= MIN_EXPECTED_FPS
    fps should be <= MAX_EXPECTED_FPS

  it should "stop updating when engine stops" in:
    gameLoop.start()
    Thread.sleep(MEDIUM_DELAY)
    val countBefore = engine.updateCount

    engine.stopEngine()
    Thread.sleep(MEDIUM_DELAY)
    val countAfter = engine.updateCount

    gameLoop.stop()

    (countAfter - countBefore) should be < MAX_UPDATES_AFTER_STOP

  it should "continue running even when engine is paused" in:
    gameLoop.start()
    Thread.sleep(SHORT_DELAY)
    val countBefore = engine.updateCount

    engine.pause()
    Thread.sleep(MEDIUM_DELAY)
    val countAfter = engine.updateCount

    gameLoop.stop()

    countAfter should be > countBefore

  it should "handle engine exceptions gracefully" in:
    engine.shouldThrow = true

    gameLoop.start()
    Thread.sleep(MEDIUM_DELAY)

    gameLoop.isRunning shouldBe true

    gameLoop.stop()

  it should "maintain consistent timing" in:
    gameLoop.start()
    Thread.sleep(EXPECTED_FRAME_TIME)
    val updateCount = engine.updateCount
    gameLoop.stop()

    val minExpected = minExpectedUpdates(EXPECTED_FRAME_TIME)
    val maxExpected = maxExpectedUpdates(EXPECTED_FRAME_TIME)

    updateCount should be >= minExpected
    updateCount should be <= maxExpected

  it should "create loop via factory" in:
    val testEngine = new TestGameEngine()
    val loop       = GameLoop.create(testEngine)

    loop shouldBe a[GameLoopImpl]
    loop.isRunning shouldBe false

// Test helper engine
class TestGameEngine extends GameEngine:
  private var _isRunning: Boolean   = true
  private var _isPaused: Boolean    = false
  private var _gameState: GameState = GameState.initial()
  var updateCount: Int              = 0
  var shouldThrow: Boolean          = false

  override def initialize(controller: it.unibo.pps.wvt.controller.GameController): Unit = {}

  override def start(): Unit = _isRunning = true
  override def stop(): Unit  = _isRunning = false
  def stopEngine(): Unit     = _isRunning = false

  override def pause(): Unit =
    _isPaused = true
    _gameState = _gameState.copy(isPaused = true)

  override def resume(): Unit =
    _isPaused = false
    _gameState = _gameState.copy(isPaused = false)

  override def update(deltaTime: Long): Unit =
    if shouldThrow then
      throw new RuntimeException("Test exception")

    if _isRunning then
      updateCount += 1
      _gameState = _gameState.copy(elapsedTime = _gameState.elapsedTime + deltaTime)

  override def isRunning: Boolean      = _isRunning
  override def isPaused: Boolean       = _isPaused
  override def currentState: GameState = _gameState
  override def updatePhase(phase: GamePhase): Unit =
    _gameState = _gameState.copy(phase = phase)

  override def getController: Option[GameController] = None
