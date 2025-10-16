package it.unibo.pps.wvt.controller.engine

import it.unibo.pps.wvt.controller.{EventHandler, GameController, GameEvent, GameSystemsState}
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

import scala.language.reflectiveCalls
import scala.reflect.Selectable.reflectiveSelectable

class GameEngineTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  var engine: GameEngine = _
  var mockController: GameController = _

  before:
    engine = new GameEngineImpl()
    mockController = createMockController()

  after:
    if engine.isRunning then engine.stop()

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

  behavior of "GameEngine"

  it should "not be running initially" in:
    engine.isRunning shouldBe false
    engine.isPaused shouldBe false

  it should "initialize with a controller" in:
    engine.initialize(mockController)
    engine.getController shouldBe defined
    engine.getController.get shouldBe mockController

  it should "start successfully when not running" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(ENGINE_STARTUP_DELAY_MS)
    engine.isRunning shouldBe true

  it should "not start when already running" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(ENGINE_STARTUP_DELAY_MS)

    val wasRunning = engine.isRunning
    engine.start() // Try to start again

    wasRunning shouldBe true
    engine.isRunning shouldBe true

  it should "stop when running" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(ENGINE_STARTUP_DELAY_MS)

    engine.stop()
    Thread.sleep(SHORT_SLEEP_MS)

    engine.isRunning shouldBe false

  it should "not stop when already stopped" in:
    engine.initialize(mockController)
    engine.isRunning shouldBe false

    engine.stop()
    engine.isRunning shouldBe false

  it should "pause when running" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(ENGINE_STARTUP_DELAY_MS)

    engine.pause()
    Thread.sleep(SHORT_SLEEP_MS)
    engine.isPaused shouldBe true
    engine.isRunning shouldBe false

  it should "not pause when not running" in:
    engine.initialize(mockController)
    engine.pause()
    engine.isPaused shouldBe false

  it should "resume from paused state" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(ENGINE_STARTUP_DELAY_MS)

    engine.pause()
    engine.isPaused shouldBe true

    engine.resume()
    Thread.sleep(SHORT_SLEEP_MS)
    engine.isPaused shouldBe false

  it should "not resume when not paused" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(ENGINE_STARTUP_DELAY_MS)

    val wasPaused = engine.isPaused
    engine.resume()

    wasPaused shouldBe false
    engine.isPaused shouldBe false

  it should "update game state when running" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(MEDIUM_SLEEP_MS)
    engine.stop()

    val finalTime = engine.currentState.elapsedTime
    finalTime should be > 0L

  it should "not update game time when paused" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(SHORT_SLEEP_MS)

    engine.pause()
    val timeWhenPaused = engine.currentState.elapsedTime

    Thread.sleep(MEDIUM_SLEEP_MS)
    val timeAfterPause = engine.currentState.elapsedTime

    engine.stop()

    timeAfterPause shouldBe timeWhenPaused +- TIME_INCREMENT

  it should "call controller update when running" in:
    val customController = createMockController()
    engine.initialize(customController)

    engine.start()
    Thread.sleep(MEDIUM_SLEEP_MS)
    engine.stop()

    customController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount should be > 0

  it should "not call controller update when paused" in:
    val customController = createMockController()
    engine.initialize(customController)

    engine.start()
    Thread.sleep(SHORT_SLEEP_MS)
    engine.pause()

    val updateCountWhenPaused = customController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    Thread.sleep(MEDIUM_SLEEP_MS)
    val updateCountAfterPause = customController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount

    engine.stop()

    updateCountAfterPause shouldBe updateCountWhenPaused +- 2

  it should "update game phase" in:
    engine.initialize(mockController)
    engine.currentState.phase shouldBe GamePhase.MainMenu

    engine.updatePhase(GamePhase.Playing)
    engine.currentState.phase shouldBe GamePhase.Playing

  it should "transition phases correctly" in:
    engine.initialize(mockController)

    engine.updatePhase(GamePhase.InfoMenu)
    engine.currentState.phase shouldBe GamePhase.InfoMenu

    engine.updatePhase(GamePhase.Playing)
    engine.currentState.phase shouldBe GamePhase.Playing

    engine.updatePhase(GamePhase.Paused)
    engine.currentState.phase shouldBe GamePhase.Paused

    engine.updatePhase(GamePhase.GameOver)
    engine.currentState.phase shouldBe GamePhase.GameOver

  it should "maintain game state across pause and resume" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(SHORT_SLEEP_MS)

    val timeBeforePause = engine.currentState.elapsedTime

    engine.pause()
    Thread.sleep(SHORT_SLEEP_MS)
    engine.resume()

    Thread.sleep(SHORT_SLEEP_MS)
    engine.stop()

    val finalTime = engine.currentState.elapsedTime
    finalTime should be > timeBeforePause

  it should "return current game state" in:
    engine.initialize(mockController)
    val state = engine.currentState

    state.phase shouldBe GamePhase.MainMenu
    state.elapsedTime shouldBe 0L
    state.isPaused shouldBe false

  it should "handle multiple start-stop cycles" in:
    engine.initialize(mockController)

    (1 to ENTITY_COUNT_FEW).foreach: _ =>
      engine.start()
      Thread.sleep(SHORT_SLEEP_MS)
      engine.isRunning shouldBe true

      engine.stop()
      Thread.sleep(SHORT_SLEEP_MS)
      engine.isRunning shouldBe false

  it should "handle multiple pause-resume cycles" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(ENGINE_STARTUP_DELAY_MS)

    (1 to ENTITY_COUNT_FEW).foreach: _ =>
      engine.pause()
      engine.isPaused shouldBe true

      engine.resume()
      Thread.sleep(DELAY_SHORT_MS)
      engine.isPaused shouldBe false

    engine.stop()

  it should "process events from event handler" in:
    engine.initialize(mockController)
    engine.start()
    Thread.sleep(MEDIUM_SLEEP_MS)
    engine.stop()

    // Events should be processed during updates
    // Verification through controller update count
    mockController.asInstanceOf[{def getUpdateCount: Int}].getUpdateCount should be > 0

  it should "maintain singleton instance through GameEngine object" in:
    val controller1 = createMockController()
    val engine1 = GameEngine.create(controller1)

    val instance = GameEngine.getInstance
    instance shouldBe defined
    instance.get shouldBe engine1

  it should "return same instance from getInstance" in:
    val controller1 = createMockController()
    GameEngine.create(controller1)

    val instance1 = GameEngine.getInstance
    val instance2 = GameEngine.getInstance

    instance1 shouldBe instance2

  behavior of "EngineState"

  it should "create initial state correctly" in:
    val state = EngineState()
    state.status shouldBe EngineStatus.Stopped
    state.gameState.phase shouldBe GamePhase.MainMenu
    state.controller shouldBe None
    state.gameLoop shouldBe None

  it should "transition to running status" in:
    val state = EngineState()
    val runningState = state.transitionTo(EngineStatus.Running)

    runningState.status shouldBe EngineStatus.Running
    runningState.gameState.isPaused shouldBe false

  it should "transition to paused status" in:
    val state = EngineState().transitionTo(EngineStatus.Running)
    val pausedState = state.transitionTo(EngineStatus.Paused)

    pausedState.status shouldBe EngineStatus.Paused
    pausedState.gameState.isPaused shouldBe true

  it should "transition to stopped status" in:
    val state = EngineState().transitionTo(EngineStatus.Running)
    val stoppedState = state.transitionTo(EngineStatus.Stopped)

    stoppedState.status shouldBe EngineStatus.Stopped
    stoppedState.gameState.isPaused shouldBe false

  it should "update game time when running" in:
    val state = EngineState().transitionTo(EngineStatus.Running)
    val updatedState = state.updateGameTime(TIME_INCREMENT)

    updatedState.gameState.elapsedTime shouldBe TIME_INCREMENT

  it should "not update game time when not running" in:
    val state = EngineState()
    val updatedState = state.updateGameTime(TIME_INCREMENT)

    updatedState.gameState.elapsedTime shouldBe 0L

  it should "update game phase" in:
    val state = EngineState()
    val updatedState = state.updatePhase(GamePhase.Playing)

    updatedState.gameState.phase shouldBe GamePhase.Playing

  it should "check if running correctly" in:
    val stoppedState = EngineState()
    val runningState = stoppedState.transitionTo(EngineStatus.Running)

    stoppedState.isRunning shouldBe false
    runningState.isRunning shouldBe true

  it should "check if paused correctly" in:
    val runningState = EngineState().transitionTo(EngineStatus.Running)
    val pausedState = runningState.transitionTo(EngineStatus.Paused)

    runningState.isPaused shouldBe false
    pausedState.isPaused shouldBe true

  it should "check if stopped correctly" in:
    val stoppedState = EngineState()
    val runningState = stoppedState.transitionTo(EngineStatus.Running)

    stoppedState.isStopped shouldBe true
    runningState.isStopped shouldBe false

  behavior of "EngineStatus"

  it should "identify running status correctly" in:
    EngineStatus.Stopped.isRunning shouldBe false
    EngineStatus.Running.isRunning shouldBe true
    EngineStatus.Paused.isRunning shouldBe false

  it should "identify paused status correctly" in:
    EngineStatus.Stopped.isPaused shouldBe false
    EngineStatus.Running.isPaused shouldBe false
    EngineStatus.Paused.isPaused shouldBe true