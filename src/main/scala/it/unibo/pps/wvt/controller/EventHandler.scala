package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.engine.GamePhase.Playing
import it.unibo.pps.wvt.view.{ViewController, ViewState}

import scala.collection.mutable

class EventHandler(engine: GameEngine) {
  private val eventQueue: EventQueue = new EventQueue()
  private val eventHandlers: mutable.Map[Class[_], GameEvent => Unit] = mutable.Map.empty

  private var currentPhase: GamePhase = GamePhase.MainMenu

  def initialize(): Unit =
    registerGameEventHandlers()


  def registerHandler[T <: GameEvent](eventClass: Class[T], handler: T => Unit): Unit =
    eventHandlers(eventClass) = handler.asInstanceOf[GameEvent => Unit]

  def postEvent(event: GameEvent): Unit =
    eventQueue.enqueue(event)

  def processEvents(): Unit =
    while (!eventQueue.isEmpty)
      eventQueue.dequeue().foreach { event =>
        processEvent(event)
      }

  def processEvent(event: GameEvent): Unit =
    eventHandlers.get(event.getClass).foreach { handler =>
      try
        handler(event)
      catch
        case e: Exception =>
          println(s"Error processing event $event: ${e.getMessage}")
          e.printStackTrace()
    }

  def clearQueue(): Unit =
    eventQueue.clear()

  def getCurrentPhase: GamePhase = currentPhase

  private def registerGameEventHandlers(): Unit =
    registerHandler(classOf[GameEvent.ShowMainMenu.type ],
      _ =>
        handleMenuTransition(GamePhase.MainMenu)
        ViewController.updateView(ViewState.MainMenu)
    )

    registerHandler(classOf[GameEvent.ShowGameView.type],
      _ =>
        handleMenuTransition(GamePhase.Playing)
        ViewController.updateView(ViewState.GameView)
        if (!engine.isRunning) engine.start()
    )

    registerHandler(classOf[GameEvent.ShowInfoMenu.type],
      _ =>
        handleMenuTransition(GamePhase.InfoMenu)
        ViewController.updateView(ViewState.InfoMenu)
    )

    registerHandler(classOf[GameEvent.ExitGame.type],
      _ =>
        engine.stop()
        sys.exit(0)
    )

    registerHandler(classOf[GameEvent.Pause.type],
      _ =>
        if (currentPhase == Playing)
          engine.pause()
          handleMenuTransition(GamePhase.Paused)
          ViewController.hideGridStatus()
    )

    registerHandler(classOf[GameEvent.Resume.type],
      _ =>
        if (currentPhase == GamePhase.Paused)
          engine.resume()
          handleMenuTransition(GamePhase.Playing)
    )

  private def handleMenuTransition(newPhase: GamePhase): Unit =
    val oldPhase = currentPhase
    currentPhase = newPhase

    engine.updatePhase(newPhase)

    (oldPhase, newPhase) match
      case (GamePhase.Playing, GamePhase.Paused) =>
        ViewController.hideGridStatus()
      case (GamePhase.Paused, GamePhase.Playing) =>
        println("Resuming game...")
      case (_, GamePhase.MainMenu) =>
        println("Returning to Main Menu...")
      case (_, GamePhase.Playing) =>
        println("Starting game...")
      case _ =>
        println(s"Transitioning from $oldPhase to $newPhase")
}

object EventHandler {
  def create(engine: GameEngine): EventHandler = {
    val handler = new EventHandler(engine)
    handler.initialize()
    handler
  }
}