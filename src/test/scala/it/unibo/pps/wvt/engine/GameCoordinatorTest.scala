package it.unibo.pps.wvt

import it.unibo.pps.wvt.controller.EventHandler
import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.model.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class GameCoordinatorTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var coordinator: GameCoordinator = _

  override def beforeEach(): Unit = {
    coordinator = GameCoordinator.getInstance
  }

  override def afterEach(): Unit = {
    coordinator.stop()
  }

  "GameCoordinator" should "initialize all systems" in {
    coordinator.getEngine shouldBe a [GameEngine]
    coordinator.getInputSystem shouldBe a [InputSystem]
    coordinator.getEventController shouldBe a [EventHandler]
  }

  it should "coordinate engine lifecycle" in {
    coordinator.getEngine.isRunning shouldBe false

    coordinator.start()
    coordinator.getEngine.isRunning shouldBe true

    coordinator.pause()
    coordinator.getEngine.isPaused shouldBe true

    coordinator.resume()
    coordinator.getEngine.isPaused shouldBe false

    coordinator.stop()
    coordinator.getEngine.isRunning shouldBe false
  }

  it should "handle mouse clicks through input system" in {
    coordinator.start()

    // Valid click
    val validX = GRID_OFFSET_X.toInt + 50
    val validY = GRID_OFFSET_Y.toInt + 50

    // Should process without errors
    coordinator.handleMouseClick(validX, validY)
    Thread.sleep(50)

    // Invalid click - should handle gracefully
    coordinator.handleMouseClick(10, 10)
    Thread.sleep(50)

    // Verify engine still running
    coordinator.getEngine.isRunning shouldBe true
  }

  it should "handle key presses" in {
    coordinator.start()
    coordinator.getEngine.isPaused shouldBe false

    // ESCAPE should pause through event controller
    coordinator.handleKeyPress("ESCAPE")
    Thread.sleep(50)
    coordinator.getEventController.processEvents()

    coordinator.getEngine.isPaused shouldBe true

    // ESCAPE again should resume
    coordinator.handleKeyPress("ESCAPE")
    Thread.sleep(50)
    coordinator.getEventController.processEvents()

    coordinator.getEngine.isPaused shouldBe false
  }

  it should "update all systems in correct order" in {
    coordinator.start()

    val initialTime = coordinator.getEngine.currentState.elapsedTime

    // Simulate update cycle
    coordinator.update(16) // ~60 FPS

    // Engine should have been updated
    coordinator.getEngine.currentState.elapsedTime should be > initialTime
  }

  it should "process inputs only when playing" in {
    // When in menu, processInputs should not be called
    coordinator.getEngine.currentState.phase shouldBe GamePhase.MainMenu

    // Update should work but not process inputs
    coordinator.update(16)

    // No errors should occur
    coordinator.getEngine.currentState.phase shouldBe GamePhase.MainMenu
  }

  it should "maintain singleton pattern" in {
    val coordinator1 = GameCoordinator.getInstance
    val coordinator2 = GameCoordinator.getInstance

    coordinator1 shouldBe theSameInstanceAs(coordinator2)
  }
  
}

class GameCoordinatorIntegrationTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var coordinator: GameCoordinator = _

  override def beforeEach(): Unit = {
    coordinator = new GameCoordinator() // New instance for each test
    coordinator.initialize()
  }

  override def afterEach(): Unit = {
    coordinator.stop()
  }

  "Complete game flow" should "work through coordinator" in {
    val engine = coordinator.getEngine
    val eventController = coordinator.getEventController

    // Start in menu
    engine.currentState.phase shouldBe GamePhase.MainMenu

    // Transition to game
    eventController.processEvent(GameEvent.ShowGameView)
    eventController.processEvents()
    Thread.sleep(100)

    engine.isRunning shouldBe true

    // Simulate game loop updates
    for (_ <- 1 to 10) {
      coordinator.update(16)
      Thread.sleep(16)
    }

    engine.currentState.elapsedTime should be > 0L

    // Handle input during game
    val clickX = GRID_OFFSET_X.toInt + 100
    val clickY = GRID_OFFSET_Y.toInt + 100
    coordinator.handleMouseClick(clickX, clickY)

    // Pause/resume
    coordinator.pause()
    engine.isPaused shouldBe true

    coordinator.resume()
    engine.isPaused shouldBe false

    // Back to menu
    eventController.processEvent(GameEvent.ShowMainMenu)
    eventController.processEvents()

    coordinator.stop()
    engine.isRunning shouldBe false
  }

  "Input to event flow" should "work correctly" in {
    coordinator.start()

    // Test complete input flow
    val inputSystem = coordinator.getInputSystem

    // 1. Validate position
    val testPosition = Position(2, 3)
    inputSystem.isValidPosition(testPosition) shouldBe true

    // 2. Handle click
    val clickX = GRID_OFFSET_X.toInt + (3 * CELL_WIDTH) + 30
    val clickY = GRID_OFFSET_Y.toInt + (2 * CELL_HEIGHT) + 30

    val clickResult = inputSystem.handleMouseClick(clickX, clickY)
    clickResult.isValid shouldBe true
    clickResult.position shouldBe testPosition

    // 3. Process through coordinator
    coordinator.handleMouseClick(clickX, clickY)

    // 4. Verify event was processed (check logs in real test)
    // For Sprint 1, just verify no crashes
  }

  "Pause state" should "persist across update cycles" in {
    coordinator.start()

    val timeBeforePause = coordinator.getEngine.currentState.elapsedTime

    coordinator.pause()

    // Multiple updates while paused
    for (_ <- 1 to 5) {
      coordinator.update(16)
      Thread.sleep(16)
    }

    // Time should not advance while paused
    coordinator.getEngine.currentState.elapsedTime shouldBe timeBeforePause

    coordinator.resume()

    // Update after resume
    coordinator.update(16)

    // Time should advance after resume
    coordinator.getEngine.currentState.elapsedTime should be > timeBeforePause
  }
}