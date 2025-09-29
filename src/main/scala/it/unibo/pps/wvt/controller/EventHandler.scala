package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.view.{ViewController, ViewState}
import scalafx.application.Platform

import scala.util.{Failure, Success, Try}

class EventHandler(engine: GameEngine):
  private val eventQueue: EventQueue = new EventQueue()
  private var eventHandlers: Map[Class[_], GameEvent => Unit] = Map.empty
  private var currentPhase: GamePhase = GamePhase.MainMenu

  def initialize(): Unit =
    registerGameEventHandlers()

  def registerHandler[T <: GameEvent](eventClass: Class[T])(handler: T => Unit): Unit =
    eventHandlers = eventHandlers + (eventClass -> handler.asInstanceOf[GameEvent => Unit])

  def postEvent(event: GameEvent): Unit =
    eventQueue.enqueue(event)

  def processEvents(): Unit =
    eventQueue.dequeueAll().foreach(processEvent)

  private def processEvent(event: GameEvent): Unit =
    val result = Try:
      event match
        case GameEvent.ShowMainMenu =>
          if currentPhase == GamePhase.Paused || currentPhase == GamePhase.Playing then
            engine.stop()
          handleMenuTransition(GamePhase.MainMenu, ViewState.MainMenu)

        case GameEvent.ShowGameView =>
          handleMenuTransition(GamePhase.Playing, ViewState.GameView)
          if !engine.isRunning then engine.start()

        case GameEvent.ShowInfoMenu =>
          handleMenuTransition(GamePhase.InfoMenu, ViewState.InfoMenu)

        case GameEvent.ExitGame =>
          engine.stop()
          Platform.runLater:
            ViewController.cleanupBeforeExit()
          Thread.sleep(100)
          Platform.exit()
          sys.exit(0)

        case GameEvent.Pause if currentPhase == GamePhase.Playing =>
          engine.pause()
          handleMenuTransition(GamePhase.Paused, ViewState.PauseMenu)

        case GameEvent.Resume if currentPhase == GamePhase.Paused =>
          engine.resume()
          handleMenuTransition(GamePhase.Playing, ViewState.GameView)
          // Force a render update to redraw existing entities
          ViewController.render()

        case _ =>
          // Fallback to registered handlers
          eventHandlers.get(event.getClass).foreach(_(event))

    result match
      case Failure(exception) =>
        println(s"Error processing event $event: ${exception.getMessage}")
        exception.printStackTrace()
      case Success(_) =>
  // Event processed successfully

  def clearQueue(): Unit =
    eventQueue.clear()

  def getCurrentPhase: GamePhase = currentPhase

  private def registerGameEventHandlers(): Unit =
    registerHandler(classOf[GameEvent.GridClicked]):
      case GameEvent.GridClicked(pos, _, _) =>
        println(s"Grid clicked at $pos")

  private def handleMenuTransition(newPhase: GamePhase, viewState: ViewState = null): Unit =
    val oldPhase = currentPhase
    currentPhase = newPhase
    engine.updatePhase(newPhase)

    Option(viewState).foreach(ViewController.updateView)

    (oldPhase, newPhase) match
      case (GamePhase.Playing, GamePhase.Paused) =>
        println("Game paused")
      case (GamePhase.Paused, GamePhase.Playing) =>
        println("Resuming game...")
      case (_, GamePhase.MainMenu) =>
        println("Returning to Main Menu...")
      case (_, GamePhase.Playing) =>
        println("Starting game...")
      case _ =>
        println(s"Transitioning from $oldPhase to $newPhase")

object EventHandler:
  def create(engine: GameEngine): EventHandler =
    val handler = new EventHandler(engine)
    handler.initialize()
    handler

  def compose(handlers: (GameEvent => Unit)*): GameEvent => Unit =
    event => handlers.foreach(_(event))

  def conditional(predicate: GameEvent => Boolean)(handler: GameEvent => Unit): GameEvent => Unit =
    event => if predicate(event) then handler(event)