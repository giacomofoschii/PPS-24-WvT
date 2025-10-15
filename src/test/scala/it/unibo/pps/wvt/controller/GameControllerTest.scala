package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameControllerTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  var world: World               = _
  var controller: GameController = _

  override def beforeEach(): Unit =
    world = World()
    controller = GameController(world)
    controller.initialize()

  override def afterEach(): Unit =
    controller.stop()
    world.clear()

  "GameController" should "initialize correctly" in:
    controller.getWorld should not be null
    controller.getEngine should not be null
    controller.getInputSystem should not be null
    controller.getEventHandler should not be null

  it should "start with initial elixir" in:
    controller.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "have access to render system" in:
    val renderSystem = controller.getRenderSystem
    renderSystem should not be null

  it should "start and stop engine" in:
    controller.getEngine.isRunning shouldBe false

    controller.start()
    controller.getEngine.isRunning shouldBe true

    controller.stop()
    controller.getEngine.isRunning shouldBe false

  it should "pause and resume game" in:
    controller.start()

    controller.pause()
    controller.getEngine.isPaused shouldBe true

    controller.resume()
    controller.getEngine.isPaused shouldBe false

  it should "post events correctly" in:
    val eventHandler = controller.getEventHandler
    val phaseBefore  = eventHandler.getCurrentPhase

    controller.postEvent(GameEvent.ShowGameView)

    // Phase should change after event processing
    eventHandler.getCurrentPhase should not be phaseBefore

  it should "update game state when running" in:
    controller.start()

    val initialTime = controller.getEngine.currentState.elapsedTime

    // Update multiple times
    for _ <- 1 to 10 do
      controller.update()
      Thread.sleep(20)

    // Time should have progressed
    controller.getEngine.currentState.elapsedTime should be > initialTime

  it should "not update when paused" in:
    controller.start()
    controller.pause()

    val pausedTime = controller.getEngine.currentState.elapsedTime

    controller.update()
    Thread.sleep(50)

    // Time should not change when paused
    controller.getEngine.currentState.elapsedTime shouldBe pausedTime

  it should "handle pause and resume correctly" in:
    controller.start()
    controller.placeWizard(WizardType.Generator, Position(2, 2))

    val beforePause = controller.getEngine.currentState.elapsedTime

    controller.pause()
    Thread.sleep(100)
    controller.update()

    val whilePaused = controller.getEngine.currentState.elapsedTime
    whilePaused shouldBe beforePause

    controller.resume()
    Thread.sleep(100)
    for _ <- 1 to 5 do
      controller.update()
      Thread.sleep(20)

    controller.getEngine.currentState.elapsedTime should be > whilePaused

  it should "reinitialize correctly" in:
    // First game session
    controller.start()
    controller.placeWizard(WizardType.Generator, Position(0, 0))
    controller.placeTroll(TrollType.Base, Position(0, 8))

    world.getAllEntities.size should be > 0

    // Reinitialize
    controller.initialize()

    // World should be cleared
    world.getAllEntities.size shouldBe 0
    controller.getCurrentElixir shouldBe INITIAL_ELIXIR
