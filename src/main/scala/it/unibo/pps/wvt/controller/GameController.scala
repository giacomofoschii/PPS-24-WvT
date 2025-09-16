package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.view.ViewController

case class GameController() {
  // Core systems
  private val gameEngine: GameEngine = GameEngine.create()
  private val eventHandler: EventHandler = EventHandler.create(gameEngine)
  private val inputSystem: InputSystem = InputSystem()
  private val viewController = ViewController
  private var renderSystem: Option[RenderSystem] = None

  def initialize(): Unit = {
    // Engine is already initialized in create()
    viewController.setController(this)
    viewController.start()
  }
  
  def update(state: GameState, deltaTime: Long): GameState = {
    // Process any pending events
    eventHandler.processEvents()

    // Process inputs (if in playing state)
    if (gameEngine.currentState.phase == GamePhase.Playing) {
      processInputs()
    }

    // Update game engine (which updates game state)
    gameEngine.update(deltaTime)

    // Trigger rendering (if render system is connected)
    renderSystem.foreach(_.render(gameEngine.currentState))

    state.copy(elapsedTime = state.elapsedTime + deltaTime)
  }
  
  def postEvent(event: GameEvent): Unit =
    eventHandler.processEvent(event)

  private def processInputs(): Unit = {
    // Input processing logic would go here
    // For Sprint 1, this is just a placeholder
    // Sprint 2 will add actual input → event conversion
  }

  def handleMouseClick(screenX: Int, screenY: Int): Unit = {
    val result = inputSystem.handleMouseClick(screenX, screenY)
    if (result.isValid) {
      val clickEvent = GameEvent.GridClicked(result.position, screenX, screenY)
      eventHandler.processEvent(clickEvent)
    }
  }

  def handleKeyPress(keyCode: String): Unit = {
    val keyEvent = GameEvent.KeyPressed(keyCode)
    eventHandler.processEvent(keyEvent)
  }

  def setRenderSystem(renderer: RenderSystem): Unit = {
    renderSystem = Some(renderer)
  }

  def start(): Unit = gameEngine.start()

  def stop(): Unit = gameEngine.stop()

  def pause(): Unit = gameEngine.pause()

  def resume(): Unit = gameEngine.resume()

  def getEngine: GameEngine = gameEngine

  def getInputSystem: InputSystem = inputSystem

  def getEventController: EventHandler = eventHandler
}

object GameController {
  private var instance: Option[GameController] = None

  def getInstance: GameController = {
    instance.getOrElse {
      val controller = new GameController()
      controller.initialize()
      instance = Some(controller)
      controller
    }
  }
}

// Placeholder for render system interface
trait RenderSystem {
  def render(gameState: GameState): Unit
}