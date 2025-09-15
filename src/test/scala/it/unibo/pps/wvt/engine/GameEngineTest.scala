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