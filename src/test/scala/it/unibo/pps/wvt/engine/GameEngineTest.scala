package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.controller.GameController
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.TestConstants._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameEngineTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  private var engine: GameEngine = _
  private var controller: GameController = _

  override def beforeEach(): Unit =
    controller = new GameController(new World())
    engine = new GameEngineImpl()
    engine.initialize(controller)

  override def afterEach(): Unit =
    if (engine.isRunning) {
      engine.stop()
    }
    Thread.sleep(SHORT_DELAY)

  "GameEngine" should "initialize with correct default state" in:
    engine.isRunning shouldBe false
    engine.isPaused shouldBe false
    engine.currentState.phase shouldBe GamePhase.MainMenu
    engine.currentState.isPaused shouldBe false
    engine.currentState.elapsedTime shouldBe INITIAL_TIME

  it should "start correctly" in:
    engine.isRunning shouldBe false

    engine.start()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false

  it should "stop correctly" in:
    engine.start()
    engine.isRunning shouldBe true

    engine.stop()
    engine.isRunning shouldBe false
    engine.isPaused shouldBe false

  it should "be idempotent on multiple starts" in:
    engine.start()
    engine.isRunning shouldBe true

    engine.start()
    engine.isRunning shouldBe true

  it should "be idempotent on multiple stops" in:
    engine.start()
    engine.stop()
    engine.isRunning shouldBe false

    engine.stop()
    engine.isRunning shouldBe false

  it should "pause and resume correctly" in:
    engine.start()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false

    engine.pause()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe true
    engine.currentState.isPaused shouldBe true

    engine.resume()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false
    engine.currentState.isPaused shouldBe false

  it should "not pause when not running" in:
    engine.isRunning shouldBe false

    engine.pause()
    engine.isPaused shouldBe false
    engine.currentState.isPaused shouldBe false


  it should "not resume when not paused" in:
    engine.start()
    engine.isPaused shouldBe false

    engine.resume()
    engine.isPaused shouldBe false

  it should "reset pause state when stopped" in:
    engine.start()
    engine.pause()
    engine.isPaused shouldBe true

    engine.stop()
    engine.isRunning shouldBe false
    engine.isPaused shouldBe false

  it should "update elapsed time when running and not paused" in:
    engine.start()

    val initialTime = engine.currentState.elapsedTime
    engine.update(STANDARD_DELTA_TIME)

    engine.currentState.elapsedTime should be >= initialTime

  it should "not update when not running" in:
    engine.isRunning shouldBe false
    val initialState = engine.currentState

    engine.update(STANDARD_DELTA_TIME)
    engine.currentState.elapsedTime shouldBe initialState.elapsedTime

  it should "not update elapsed time when paused" in:
    engine.start()
    engine.pause()

    val pausedTime = engine.currentState.elapsedTime
    engine.update(STANDARD_DELTA_TIME)
    engine.currentState.elapsedTime shouldBe pausedTime

  it should "update game phase correctly" in:
    engine.currentState.phase shouldBe GamePhase.MainMenu

    engine.updatePhase(GamePhase.Playing)
    engine.currentState.phase shouldBe GamePhase.Playing

    engine.updatePhase(GamePhase.Paused)
    engine.currentState.phase shouldBe GamePhase.Paused

  it should "handle multiple pause/resume cycles" in:
    engine.start()

    for (_ <- 1 to SMALL_ITERATION_COUNT)
      engine.pause()
      engine.isPaused shouldBe true

      engine.resume()
      engine.isPaused shouldBe false

    engine.isRunning shouldBe true
    engine.isPaused shouldBe false

  it should "maintain state consistency during lifecycle" in:
    engine.isRunning shouldBe false
    engine.isPaused shouldBe false
    engine.currentState.phase shouldBe GamePhase.MainMenu

    engine.start()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false

    engine.update(SHORT_DELAY)
    engine.currentState.elapsedTime should be > INITIAL_TIME

    engine.pause()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe true

    engine.resume()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false

    engine.stop()
    engine.isRunning shouldBe false
    engine.isPaused shouldBe false