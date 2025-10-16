package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameControllerTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  var world: World = _
  var controller: GameController = _

  override def beforeEach(): Unit =
    world = World()
    controller = GameController(world)
    controller.initialize()

  override def afterEach(): Unit =
    controller.stop()
    world.clear()

  "A GameController" should "initialize correctly" in:
    controller.getWorld should not be null
    controller.getEngine should not be null
    controller.getInputSystem should not be null
    controller.getEventHandler should not be null

  it should "start with the correct initial amount of elixir" in:
    controller.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "provide access to the render system" in:
    controller.getRenderSystem should not be null

  // --- Engine Control Tests ---

  it should "start and stop the game engine" in:
    controller.getEngine.isRunning shouldBe false
    controller.start()
    controller.getEngine.isRunning shouldBe true
    controller.stop()
    controller.getEngine.isRunning shouldBe false

  it should "pause and resume the game engine" in:
    controller.start()
    controller.getEngine.isPaused shouldBe false
    controller.pause()
    controller.getEngine.isPaused shouldBe true
    controller.resume()
    controller.getEngine.isPaused shouldBe false

  // --- Game State Update Tests ---

  it should "update the game state over time when running" in:
    controller.start()
    val initialTime = controller.getEngine.currentState.elapsedTime

    for (_ <- 1 to UPDATES_COUNT_MEDIUM)
      controller.update()
      Thread.sleep(DELAY_FRAME_MS)

    controller.getEngine.currentState.elapsedTime should be > initialTime

  it should "not update the game state when paused" in:
    controller.start()
    controller.pause()
    val pausedTime = controller.getEngine.currentState.elapsedTime

    controller.update()
    Thread.sleep(DELAY_SHORT_MS)

    controller.getEngine.currentState.elapsedTime shouldBe pausedTime