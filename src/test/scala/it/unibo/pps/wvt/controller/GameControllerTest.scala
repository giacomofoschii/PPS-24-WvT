package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.controller.*
import it.unibo.pps.wvt.engine.{GameEngine, GamePhase, GameState}
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.model.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

object GameControllerTestConstants {
  // Time constants (in milliseconds)
  val SHORT_DELAY: Long = 50L
  val MEDIUM_DELAY: Long = 100L
  val LONG_DELAY: Long = 200L
  val STANDARD_DELTA_TIME: Long = 16L // ~60 FPS

  // Grid test positions
  val TEST_GRID_OFFSET: Int = 50
  val TEST_CELL_OFFSET: Int = 30
  val INVALID_GRID_OFFSET: Int = 10

  // Test position indices
  val TEST_ROW_INDEX: Int = 2
  val TEST_COL_INDEX: Int = 3
  val ZERO_INDEX: Int = 0

  // Loop iterations
  val SMALL_LOOP_COUNT: Int = 5
  val MEDIUM_LOOP_COUNT: Int = 10
  val LARGE_LOOP_COUNT: Int = 20

  // Expected values
  val ZERO_TIME: Long = 0L
  val MIN_FPS: Int = 50
  val MAX_FPS: Int = 70
  val TARGET_FPS_VALUE: Int = 60
}

import GameControllerTestConstants.*

class GameControllerTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var controller: GameController = _

  override def beforeEach(): Unit = 
    controller = GameController()

  override def afterEach(): Unit = 
    controller.stop()
    Thread.sleep(SHORT_DELAY)

  "GameController" should "initialize all core systems" in {
    controller.getEngine shouldBe a [GameEngine]
    controller.getInputSystem shouldBe a [InputSystem]
    controller.getEventController shouldBe a [EventHandler]
  }

  it should "start with engine in stopped state" in {
    controller.getEngine.isRunning shouldBe false
    controller.getEngine.isPaused shouldBe false
  }

  it should "coordinate engine lifecycle correctly" in {
    // Initial state
    controller.getEngine.isRunning shouldBe false

    // Start
    controller.start()
    controller.getEngine.isRunning shouldBe true
    controller.getEngine.isPaused shouldBe false

    // Pause
    controller.pause()
    controller.getEngine.isPaused shouldBe true
    controller.getEngine.isRunning shouldBe true

    // Resume
    controller.resume()
    controller.getEngine.isPaused shouldBe false
    controller.getEngine.isRunning shouldBe true

    // Stop
    controller.stop()
    controller.getEngine.isRunning shouldBe false
    controller.getEngine.isPaused shouldBe false
  }

  it should "handle valid mouse clicks through input system" in {
    controller.start()

    // Calculate valid click position
    val validX = GRID_OFFSET_X.toInt + TEST_GRID_OFFSET
    val validY = GRID_OFFSET_Y.toInt + TEST_GRID_OFFSET

    // Should process without errors
    controller.handleMouseClick(validX, validY)
    Thread.sleep(SHORT_DELAY)
    controller.getEventController.processEvents()

    // Verify engine still running
    controller.getEngine.isRunning shouldBe true
  }

  it should "handle invalid mouse clicks gracefully" in {
    controller.start()

    // Click outside grid - should handle gracefully
    controller.handleMouseClick(INVALID_GRID_OFFSET, INVALID_GRID_OFFSET)
    Thread.sleep(SHORT_DELAY)
    controller.getEventController.processEvents()

    // Verify engine still running
    controller.getEngine.isRunning shouldBe true
  }

  it should "handle key presses correctly" in {
    controller.start()
    controller.getEngine.isPaused shouldBe false

    // ESCAPE should pause
    controller.handleKeyPress("ESCAPE")
    Thread.sleep(SHORT_DELAY)
    controller.getEventController.processEvents()
    controller.getEngine.isPaused shouldBe true

    // ESCAPE again should resume
    controller.handleKeyPress("ESCAPE")
    Thread.sleep(SHORT_DELAY)
    controller.getEventController.processEvents()
    controller.getEngine.isPaused shouldBe false
  }

  it should "process events through event handler" in {
    val eventHandler = controller.getEventController

    // Post a pause event
    controller.postEvent(GameEvent.Pause)
    eventHandler.processEvents()

    controller.getEngine.isPaused shouldBe true

    // Post a resume event
    controller.postEvent(GameEvent.Resume)
    eventHandler.processEvents()

    controller.getEngine.isPaused shouldBe false
  }

  it should "update game state with delta time" in {
    controller.start()

    val initialState = controller.getEngine.currentState
    val initialTime = initialState.elapsedTime

    // Update with standard frame time
    controller.update(initialState, STANDARD_DELTA_TIME)

    // Process any pending events
    controller.getEventController.processEvents()

    // Time should have advanced
    val updatedState = controller.getEngine.currentState
    updatedState.elapsedTime should be >= initialTime
  }

  it should "not process inputs when in menu phase" in {
    // Initial state should be MainMenu
    controller.getEngine.currentState.phase shouldBe GamePhase.MainMenu

    // Update should work but not process inputs
    val state = controller.getEngine.currentState
    controller.update(state, STANDARD_DELTA_TIME)

    // Should still be in menu
    controller.getEngine.currentState.phase shouldBe GamePhase.MainMenu
  }

  it should "process inputs only when in playing phase" in {
    // Transition to playing phase
    controller.postEvent(GameEvent.ShowGameView)
    controller.getEventController.processEvents()
    Thread.sleep(MEDIUM_DELAY)

    controller.getEngine.isRunning shouldBe true

    // Now inputs should be processed during update
    val state = controller.getEngine.currentState.copy(phase = GamePhase.Playing)
    controller.update(state, STANDARD_DELTA_TIME)

    // Verify no errors occurred
    controller.getEngine.isRunning shouldBe true
  }

  it should "set and use render system" in {
    var renderCalled = false

    val testRenderer = new RenderSystem {
      def render(gameState: GameState): Unit = {
        renderCalled = true
      }
    }

    controller.setRenderSystem(testRenderer)
    controller.start()

    // Update should trigger render
    val state = controller.getEngine.currentState
    controller.update(state, STANDARD_DELTA_TIME)

    renderCalled shouldBe true
  }

  it should "handle multiple rapid updates" in {
    controller.start()

    val initialTime = controller.getEngine.currentState.elapsedTime

    // Simulate rapid updates
    for (_ <- ZERO_INDEX until MEDIUM_LOOP_COUNT) {
      val state = controller.getEngine.currentState
      controller.update(state, STANDARD_DELTA_TIME)
      Thread.sleep(STANDARD_DELTA_TIME)
    }

    val finalTime = controller.getEngine.currentState.elapsedTime
    finalTime should be > initialTime
  }

  it should "handle grid click events correctly" in {
    controller.start()

    // Calculate position for specific grid cell
    val targetCol = TEST_COL_INDEX
    val targetRow = TEST_ROW_INDEX

    val clickX = GRID_OFFSET_X.toInt + (targetCol * CELL_WIDTH) + TEST_CELL_OFFSET
    val clickY = GRID_OFFSET_Y.toInt + (targetRow * CELL_HEIGHT) + TEST_CELL_OFFSET

    controller.handleMouseClick(clickX, clickY)
    Thread.sleep(SHORT_DELAY)
    controller.getEventController.processEvents()

    // Verify engine still running
    controller.getEngine.isRunning shouldBe true
  }
}

class GameControllerIntegrationTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var controller: GameController = _

  override def beforeEach(): Unit =
    controller = GameController()

  override def afterEach(): Unit =
    controller.stop()
    Thread.sleep(SHORT_DELAY)

  "Complete game flow" should "work through controller" in {
    val engine = controller.getEngine
    val eventController = controller.getEventController

    // Start in menu
    engine.currentState.phase shouldBe GamePhase.MainMenu

    // Transition to game
    eventController.processEvent(GameEvent.ShowGameView)
    eventController.processEvents()
    Thread.sleep(MEDIUM_DELAY)

    engine.isRunning shouldBe true

    // Simulate game loop updates
    for (_ <- ZERO_INDEX until MEDIUM_LOOP_COUNT) {
      val state = engine.currentState
      controller.update(state, STANDARD_DELTA_TIME)
      Thread.sleep(STANDARD_DELTA_TIME)
    }

    engine.currentState.elapsedTime should be > ZERO_TIME

    // Handle input during game
    val clickX = GRID_OFFSET_X.toInt + (CELL_WIDTH * TEST_COL_INDEX)
    val clickY = GRID_OFFSET_Y.toInt + (CELL_HEIGHT * TEST_ROW_INDEX)
    controller.handleMouseClick(clickX, clickY)

    // Pause/resume cycle
    controller.pause()
    engine.isPaused shouldBe true

    controller.resume()
    engine.isPaused shouldBe false

    // Back to menu
    eventController.processEvent(GameEvent.ShowMainMenu)
    eventController.processEvents()

    controller.stop()
    engine.isRunning shouldBe false
  }

  "Input to event flow" should "work correctly" in {
    controller.start()

    val inputSystem = controller.getInputSystem

    // Test complete input flow
    // 1. Create and validate position
    val testPosition = Position(TEST_ROW_INDEX, TEST_COL_INDEX)
    inputSystem.isValidPosition(testPosition) shouldBe true

    // 2. Calculate click coordinates for the position
    val clickX = GRID_OFFSET_X.toInt + (TEST_COL_INDEX * CELL_WIDTH) + TEST_CELL_OFFSET
    val clickY = GRID_OFFSET_Y.toInt + (TEST_ROW_INDEX * CELL_HEIGHT) + TEST_CELL_OFFSET

    // 3. Validate click result
    val clickResult = inputSystem.handleMouseClick(clickX, clickY)
    clickResult.isValid shouldBe true
    clickResult.position shouldBe testPosition

    // 4. Process through controller
    controller.handleMouseClick(clickX, clickY)
    Thread.sleep(SHORT_DELAY)
    controller.getEventController.processEvents()

    // 5. Verify no crashes occurred
    controller.getEngine.isRunning shouldBe true
  }

  "Pause state" should "persist across update cycles" in {
    controller.start()
    Thread.sleep(SHORT_DELAY)

    // Let some time pass
    for (_ <- ZERO_INDEX until SMALL_LOOP_COUNT) {
      val state = controller.getEngine.currentState
      controller.update(state, STANDARD_DELTA_TIME)
    }

    val timeBeforePause = controller.getEngine.currentState.elapsedTime
    timeBeforePause should be > ZERO_TIME

    // Pause the game
    controller.pause()
    controller.getEngine.isPaused shouldBe true

    // Multiple updates while paused
    for (_ <- ZERO_INDEX until SMALL_LOOP_COUNT) {
      val state = controller.getEngine.currentState
      controller.update(state, STANDARD_DELTA_TIME)
      Thread.sleep(STANDARD_DELTA_TIME)
    }

    // Time should not advance while paused
    controller.getEngine.currentState.elapsedTime shouldBe timeBeforePause

    // Resume the game
    controller.resume()
    controller.getEngine.isPaused shouldBe false

    // Update after resume
    val state = controller.getEngine.currentState
    controller.update(state, STANDARD_DELTA_TIME)

    // Time should advance after resume
    controller.getEngine.currentState.elapsedTime should be > timeBeforePause
  }

  "Event processing" should "handle menu transitions" in {
    val eventController = controller.getEventController

    // Start in MainMenu
    controller.getEngine.currentState.phase shouldBe GamePhase.MainMenu

    // Transition to InfoMenu
    eventController.processEvent(GameEvent.ShowInfoMenu)
    eventController.processEvents()
    Thread.sleep(SHORT_DELAY)

    // Transition to GameView
    eventController.processEvent(GameEvent.ShowGameView)
    eventController.processEvents()
    Thread.sleep(SHORT_DELAY)

    controller.getEngine.isRunning shouldBe true

    // Back to MainMenu
    eventController.processEvent(GameEvent.ShowMainMenu)
    eventController.processEvents()
    Thread.sleep(SHORT_DELAY)
  }

  "Controller" should "handle rapid event sequences" in {
    controller.start()

    // Rapid pause/resume sequence
    for (_ <- ZERO_INDEX until SMALL_LOOP_COUNT) {
      controller.pause()
      Thread.sleep(SHORT_DELAY)
      controller.resume()
      Thread.sleep(SHORT_DELAY)
    }

    // Should end in resumed state
    controller.getEngine.isPaused shouldBe false
    controller.getEngine.isRunning shouldBe true
  }

  "Mouse clicks" should "be processed at different grid positions" in {
    controller.start()

    // Test corners
    val corners = List(
      (ZERO_INDEX, ZERO_INDEX),
      (ZERO_INDEX, GRID_COLS - 1),
      (GRID_ROWS - 1, ZERO_INDEX),
      (GRID_ROWS - 1, GRID_COLS - 1)
    )

    corners.foreach { case (row, col) =>
      val clickX = GRID_OFFSET_X.toInt + (col * CELL_WIDTH) + (CELL_WIDTH / 2)
      val clickY = GRID_OFFSET_Y.toInt + (row * CELL_HEIGHT) + (CELL_HEIGHT / 2)

      controller.handleMouseClick(clickX, clickY)
      Thread.sleep(SHORT_DELAY)
    }

    controller.getEventController.processEvents()

    // Verify system still functional
    controller.getEngine.isRunning shouldBe true
  }
}