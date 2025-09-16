package it.unibo.pps.wvt.engine

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameEngineTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  private val DELTA_TIME: Long = 100L

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
    engine.isPaused shouldBe false
    engine.currentState.phase shouldBe GamePhase.MainMenu
    engine.currentState.isPaused shouldBe false
    engine.currentState.elapsedTime shouldBe 0L
  }

  it should "start and stop correctly" in {
    engine.isRunning shouldBe false

    engine.start()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false

    engine.stop()
    engine.isRunning shouldBe false
    engine.isPaused shouldBe false
  }

  it should "not start multiple times" in {
    engine.start()
    engine.isRunning shouldBe true

    engine.start() // Should be idempotent
    engine.isRunning shouldBe true
  }

  it should "pause and resume correctly" in {
    engine.start()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false

    engine.pause()
    engine.isRunning shouldBe true // Still running
    engine.isPaused shouldBe true
    engine.currentState.isPaused shouldBe true

    engine.resume()
    engine.isRunning shouldBe true
    engine.isPaused shouldBe false
    engine.currentState.isPaused shouldBe false
  }

  it should "not pause when not running" in {
    engine.isRunning shouldBe false

    engine.pause()

    engine.isPaused shouldBe false
    engine.currentState.isPaused shouldBe false
  }

  it should "reset pause state when stopped" in {
    engine.start()
    engine.pause()
    engine.isPaused shouldBe true

    engine.stop()

    engine.isRunning shouldBe false
    engine.isPaused shouldBe false
  }

  it should "update elapsed time when running and not paused" in {
    engine.start()

    val initialTime = engine.currentState.elapsedTime
    engine.update(DELTA_TIME)
    engine.currentState.elapsedTime shouldBe (initialTime + DELTA_TIME)
  }

  it should "not update when not running" in {
    engine.isRunning shouldBe false

    val initialState = engine.currentState
    engine.update(DELTA_TIME)

    engine.currentState shouldBe initialState
  }

  it should "not update when paused" in {
    engine.start()
    engine.pause()

    val pausedTime = engine.currentState.elapsedTime
    engine.update(DELTA_TIME)

    engine.currentState.elapsedTime shouldBe pausedTime
  }

  it should "handle pause/resume/pause sequence" in {
    engine.start()

    engine.pause()
    engine.isPaused shouldBe true

    engine.resume()
    engine.isPaused shouldBe false

    engine.pause()
    engine.isPaused shouldBe true
  }

  "GameEngine factory" should "maintain singleton instance" in {
    val engine1 = GameEngine.getInstance
    val engine2 = GameEngine.getInstance

    engine1 shouldBe defined
    engine2 shouldBe defined
    engine1.get shouldBe theSameInstanceAs(engine2.get)
  }
}