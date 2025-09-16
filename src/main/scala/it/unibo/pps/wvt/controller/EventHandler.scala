package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine._
import it.unibo.pps.wvt.model.Position

/**
 * Controller for game events
 * Handles all game-specific events while GameEngine handles only lifecycle
 */
class EventHandler(engine: GameEngine) {

  private val eventProcessor: EventProcessor = new EventProcessor()

  def initialize(): Unit = {
    registerEventHandlers()
  }

  private def registerEventHandlers(): Unit = {
    // Menu navigation events
    eventProcessor.registerHandler(classOf[GameEvent.ShowMainMenu.type],
      _ => handleMenuTransition(GamePhase.MainMenu))

    eventProcessor.registerHandler(classOf[GameEvent.ShowGameView.type],
      _ => {
        handleMenuTransition(GamePhase.Playing)
        if (!engine.isRunning) engine.start()
      })

    eventProcessor.registerHandler(classOf[GameEvent.ShowInfoMenu.type],
      _ => handleMenuTransition(GamePhase.InfoMenu))

    eventProcessor.registerHandler(classOf[GameEvent.ExitGame.type],
      _ => {
        engine.stop()
        sys.exit(0)
      })

    // Game control events
    eventProcessor.registerHandler(classOf[GameEvent.Pause.type],
      _ => engine.pause())

    eventProcessor.registerHandler(classOf[GameEvent.Resume.type],
      _ => engine.resume())

    // Input events (Sprint 1 - just logging)
    eventProcessor.registerHandler(classOf[GameEvent.GridClicked],
      event => {
        val clickEvent = event.asInstanceOf[GameEvent.GridClicked]
        println(s"Grid clicked at: ${clickEvent.pos}")
        // Sprint 2 will add entity placement logic
      })

    eventProcessor.registerHandler(classOf[GameEvent.KeyPressed],
      event => {
        val keyEvent = event.asInstanceOf[GameEvent.KeyPressed]
        keyEvent.keyCode match {
          case "ESCAPE" => if (engine.isPaused) engine.resume() else engine.pause()
          case _ => println(s"Key pressed: ${keyEvent.keyCode}")
        }
      })
  }

  private def handleMenuTransition(newPhase: GamePhase): Unit = {
    // Update phase in game state
    // This would be done through a proper state manager in production
    println(s"Transitioning to: $newPhase")
  }

  def processEvent(event: GameEvent): Unit = {
    eventProcessor.postEvent(event)
  }

  def processEvents(): Unit = {
    eventProcessor.processEvents()
  }
}

object EventHandler {
  def create(engine: GameEngine): EventHandler = {
    val handler = new EventHandler(engine)
    handler.initialize()
    handler
  }
}