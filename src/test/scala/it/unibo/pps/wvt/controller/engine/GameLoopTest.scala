package it.unibo.pps.wvt.controller.engine

import it.unibo.pps.wvt.controller.*
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.engine.LoopStatus.LoopState
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

import scala.language.reflectiveCalls
import scala.reflect.Selectable.reflectiveSelectable

class GameLoopTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  var engine: GameEngine = _
  var gameLoop: GameLoop = _
  var mockController: GameController = _

  before:
    engine = new GameEngineImpl()
    mockController = createMockController()
    engine.initialize(mockController)
    gameLoop = GameLoop.create(engine)

  after:
    if gameLoop.isRunning then gameLoop.stop()

  def createMockController(): GameController =
    new GameController(World.empty):
      private var updateCount = 0
      private val eventHandler: EventHandler = new EventHandler:
        override def processEvents(): List[GameEvent] = List.empty
        override def postEvent(event: GameEvent): Unit = ()
        override def registerHandler[T <: GameEvent](eventClass: Class[T])(handler: T => Unit): Unit = ()
        override def getCurrentPhase: GamePhase = GamePhase.MainMenu
        override def clearQueue(): Unit = ()

      override def getWorld: World = World.empty
      override def update(): Unit = updateCount += 1
      override def getEventHandler: EventHandler = eventHandler
      def getSystemsState: GameSystemsState = GameSystemsState.initial(WAVE_FIRST)
      def getUpdateCount: Int = updateCount

  behavior of "GameLoop"

  it should "not be running initially" in:
    gameLoop.isRunning shouldBe false

  it should "start successfully" in:
    gameLoop.start()
    Thread.sleep(LOOP_STARTUP_DELAY_MS)
    gameLoop.isRunning shouldBe true
    gameLoop.stop()

  it should "not start when already running" in:
    gameLoop.start()
    Thread.sleep(LOOP_STARTUP_DELAY_MS)

    val wasRunning = gameLoop.isRunning
    gameLoop.start() // Try to start again

    wasRunning shouldBe true
    gameLoop.isRunning shouldBe true
    gameLoop.stop()

  it should "stop when running" in:
    gameLoop.start()
    Thread.sleep(LOOP_STARTUP_DELAY_MS)

    gameLoop.stop()
    Thread.sleep(LOOP_STARTUP_DELAY_MS)

    gameLoop.isRunning shouldBe false

  it should "not stop when already stopped" in:
    gameLoop.isRunning shouldBe false
    gameLoop.stop()
    gameLoop.isRunning shouldBe false

  it should "update engine when running" in:
    engine.start()
    gameLoop.start()

    Thread.sleep(SHORT_RUN_MS)

    gameLoop.stop()
    engine.stop()

    val updateCount = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount
    updateCount should be > 0

  it should "not update engine when stopped" in:
    gameLoop.start()
    Thread.sleep(LOOP_STARTUP_DELAY_MS)

    gameLoop.stop()
    Thread.sleep(LOOP_STARTUP_DELAY_MS)

    val updateCountAfterStop = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    Thread.sleep(MEDIUM_RUN_MS)
    val updateCountAfterWait = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    updateCountAfterWait shouldBe updateCountAfterStop +- 2

  it should "calculate FPS correctly" in:
    engine.start()
    gameLoop.start()

    Thread.sleep(FPS_MEASUREMENT_DELAY_MS)

    val fps = gameLoop.getCurrentFps

    gameLoop.stop()
    engine.stop()

    fps should (be >= EXPECTED_FPS_MIN and be <= EXPECTED_FPS_MAX)

  it should "initialize FPS to zero" in:
    gameLoop.getCurrentFps shouldBe 0

  it should "not update when engine is paused" in:
    engine.start()
    gameLoop.start()

    Thread.sleep(SHORT_RUN_MS)
    engine.pause()

    val updateCountWhenPaused = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    Thread.sleep(MEDIUM_RUN_MS)
    val updateCountAfterPause = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    gameLoop.stop()
    engine.stop()

    updateCountAfterPause shouldBe updateCountWhenPaused +- 2

  it should "resume updating when engine is resumed" in:
    engine.start()
    gameLoop.start()

    Thread.sleep(SHORT_RUN_MS)
    engine.pause()
    Thread.sleep(SHORT_RUN_MS)
    engine.resume()

    val updateCountAfterResume = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    Thread.sleep(SHORT_RUN_MS)
    val finalUpdateCount = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    gameLoop.stop()
    engine.stop()

    finalUpdateCount should be > updateCountAfterResume

  it should "handle multiple start-stop cycles" in:
    (1 to ENTITY_COUNT_FEW).foreach: _ =>
      gameLoop.start()
      Thread.sleep(LOOP_STARTUP_DELAY_MS)
      gameLoop.isRunning shouldBe true

      gameLoop.stop()
      Thread.sleep(LOOP_STARTUP_DELAY_MS)
      gameLoop.isRunning shouldBe false

  it should "maintain consistent update rate" in:
    engine.start()
    gameLoop.start()

    Thread.sleep(MEDIUM_RUN_MS)
    val updateCount1 = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    Thread.sleep(MEDIUM_RUN_MS)
    val updateCount2 = mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    gameLoop.stop()
    engine.stop()

    val updatesInPeriod = updateCount2 - updateCount1
    updatesInPeriod should be > 0

  it should "process events from engine" in:
    engine.start()
    gameLoop.start()

    Thread.sleep(SHORT_RUN_MS)

    gameLoop.stop()
    engine.stop()

    // Events should be processed during updates
    mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount should be > 0

  behavior of "LoopState"

  it should "create initial state correctly" in:

    val state = LoopState()
    state.status shouldBe LoopStatus.Idle
    state.lastUpdate shouldBe 0L
    state.accumulator shouldBe 0L
    state.frameCount shouldBe 0
    state.currentFps shouldBe 0
    state.isPaused shouldBe false

  it should "transition to running state" in:

    val state = LoopState()
    val runningState = state.startRunning

    runningState.status shouldBe LoopStatus.Running
    runningState.lastUpdate should be > 0L
    runningState.accumulator shouldBe 0L
    runningState.frameCount shouldBe 0

  it should "transition to stopped state" in:

    val state = LoopState().startRunning
    val stoppedState = state.stopRunning

    stoppedState.status shouldBe LoopStatus.Idle
    stoppedState.isPaused shouldBe false

  it should "mark as paused" in:

    val state = LoopState().startRunning
    val pausedState = state.markPaused()

    pausedState.isPaused shouldBe true
    pausedState.accumulator shouldBe 0L

  it should "resume from pause" in:

    val state = LoopState().startRunning.markPaused()
    val currentTime = System.nanoTime()
    val resumedState = state.resumeFromPause(currentTime)

    resumedState.isPaused shouldBe false
    resumedState.lastUpdate shouldBe currentTime
    resumedState.accumulator shouldBe 0L

  it should "update frame correctly" in:

    val state = LoopState().startRunning
    val currentTime = System.nanoTime()
    val updatedState = state.updateFrame(currentTime)

    updatedState.lastUpdate shouldBe currentTime
    updatedState.accumulator should be >= 0L

  it should "consume time step from accumulator" in:

    val state = LoopState(accumulator = 100L)
    val consumedState = state.consumeTimeStep(16L)

    consumedState.accumulator shouldBe 84L

  it should "increment frame count" in:

    val state = LoopState()
    val incrementedState = state.incrementFrameCount

    incrementedState.frameCount shouldBe 1

  it should "update FPS after one second" in:

    val currentTime = System.currentTimeMillis()
    val state = LoopState(frameCount = 60, fpsTimer = currentTime - 1000L)
    val updatedState = state.updateFps(currentTime)

    updatedState.currentFps shouldBe 60
    updatedState.frameCount shouldBe 0
    updatedState.fpsTimer shouldBe currentTime

  it should "not update FPS before one second" in:

    val currentTime = System.currentTimeMillis()
    val state = LoopState(frameCount = 30, fpsTimer = currentTime - 500L, currentFps = 60)
    val updatedState = state.updateFps(currentTime)

    updatedState.currentFps shouldBe 60
    updatedState.frameCount shouldBe 30

  it should "check if running correctly" in:

    val stoppedState = LoopState()
    val runningState = stoppedState.startRunning

    stoppedState.isRunning shouldBe false
    runningState.isRunning shouldBe true

  it should "check if has accumulated time correctly" in:

    val state1 = LoopState(accumulator = 20L)
    val state2 = LoopState(accumulator = 10L)

    state1.hasAccumulatedTime(16L) shouldBe true
    state2.hasAccumulatedTime(16L) shouldBe false

  behavior of "LoopStatus"

  it should "identify running status correctly" in:
    LoopStatus.Idle.isRunning shouldBe false
    LoopStatus.Running.isRunning shouldBe true
    LoopStatus.Stopping.isRunning shouldBe false

  it should "create loop with custom engine" in:
    val customEngine = new GameEngineImpl()
    val customController = createMockController()
    customEngine.initialize(customController)

    val customLoop = GameLoop.create(customEngine)
    customLoop should not be null
    customLoop.isRunning shouldBe false