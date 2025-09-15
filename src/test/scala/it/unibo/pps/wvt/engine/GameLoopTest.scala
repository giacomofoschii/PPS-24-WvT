package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.utilities.GameConstants.*
import it.unibo.pps.wvt.utilities.TestConstants._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameLoopTest extends AnyFlatSpec with Matchers {

  "GameLoopConfig" should "have correct default values" in {
    val engine = GameEngine.create()
    val loop = new EventBasedGameLoop(engine)

    loop.targetFps shouldBe TARGET_FPS
    loop.tickRate shouldBe (1000 / TARGET_FPS)
  }

  it should "calculate tickRate from custom FPS" in {
    val engine = GameEngine.create()
    val loop = GameLoop.withCustomFPS(engine, TEST_FPS_LOW)

    loop.targetFps shouldBe TEST_FPS_LOW
    loop.tickRate shouldBe (1000 / TEST_FPS_LOW)
  }

  "EventBasedGameLoop" should "start and stop correctly" in {
    val engine = GameEngine.create()
    val loop = GameLoop.create(engine)

    loop.isRunning shouldBe false

    loop.start()
    loop.isRunning shouldBe true

    loop.stop()
    Thread.sleep(SMALL_DELAY)
    loop.isRunning shouldBe false
  }

  it should "update engine periodically" in {
    val engine = GameEngine.create()
    engine.processEvent(GameEvent.StartWave)

    val initialState = engine.currentState
    val loop = GameLoop.withCustomFPS(engine, TEST_FPS_VERY_LOW)

    loop.start()
    Thread.sleep(TEST_DURATION_HALF_SEC)
    loop.stop()

    // State should have changed
    engine.currentState should not equal initialState
  }
}

class GameLoopFactoryTest extends AnyFlatSpec with Matchers {

  "GameLoop object" should "create default game loop" in {
    val engine = GameEngine.create()
    val loop = GameLoop.create(engine)

    loop shouldBe a [EventBasedGameLoop]
    loop.targetFps shouldBe TARGET_FPS
  }

  it should "create loop with custom FPS" in {
    val engine = GameEngine.create()
    val loop = GameLoop.withCustomFPS(engine, TEST_FPS_MEDIUM)

    loop shouldBe a [EventBasedGameLoop]
    loop.targetFps shouldBe TEST_FPS_MEDIUM
    loop.tickRate shouldBe (1000 / TEST_FPS_MEDIUM)
  }
}