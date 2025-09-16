package it.unibo.pps.wvt

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.controller.EventHandler
import it.unibo.pps.wvt.input.InputSystem

/**
 * Main game coordinator - ties everything together
 * This is the main integration point for all systems
 */
class GameCoordinator {

  // Core systems
  private val gameEngine: GameEngine = GameEngine.create()
  private val eventHandler: EventHandler = EventHandler.create(gameEngine)
  private val inputSystem: InputSystem = InputSystem()
  private var renderSystem: Option[RenderSystem] = None

  def initialize(): Unit = {
    // Engine is already initialized in create()
    println("Game Coordinator initialized")
  }

  /**
   * Main update method called by the game loop
   * This is where all systems are coordinated
   */
  def update(deltaTime: Long): Unit = {
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
  }

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

object GameCoordinator {
  private var instance: Option[GameCoordinator] = None

  def getInstance: GameCoordinator = {
    instance.getOrElse {
      val coordinator = new GameCoordinator()
      coordinator.initialize()
      instance = Some(coordinator)
      coordinator
    }
  }
}

// Placeholder for render system interface
trait RenderSystem {
  def render(gameState: GameState): Unit
}