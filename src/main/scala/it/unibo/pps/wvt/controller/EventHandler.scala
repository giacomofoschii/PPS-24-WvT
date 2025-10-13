package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.controller.GameEvent.*
import it.unibo.pps.wvt.engine.{GamePhase, *}
import it.unibo.pps.wvt.engine.GamePhase.*
import it.unibo.pps.wvt.view.{ViewController, ViewState}
import scalafx.application.Platform

import java.util.concurrent.atomic.AtomicReference
import scala.util.{Failure, Success, Try}

case class EventHandlerState(
                              eventQueue: EventQueue = EventQueue.empty,
                              eventHandlers: Map[Class[_], GameEvent => Unit] = Map.empty,
                              currentPhase: GamePhase = GamePhase.MainMenu
                            )

trait EventHandler:
  def postEvent(event: GameEvent): Unit
  def processEvents(): List[GameEvent]
  def registerHandler[T <: GameEvent](eventClass: Class[T])(handler: T => Unit): Unit
  def getCurrentPhase: GamePhase
  def clearQueue(): Unit

class EventHandlerImpl(private val engine: GameEngine) extends EventHandler:

  private val stateRef = new AtomicReference(EventHandlerState())

  override def postEvent(event: GameEvent): Unit =
    updateState(_.copy(eventQueue = stateRef.get().eventQueue.enqueue(event)))

  override def processEvents(): List[GameEvent] =
    val currentState = stateRef.get()
    val (newQueue, events) = currentState.eventQueue.dequeueAll()
    events.foreach(processEvent)
    updateState(_.copy(eventQueue = newQueue))
    events

  override def registerHandler[T <: GameEvent](eventClass: Class[T])(handler: T => Unit): Unit =
    updateState: state =>
      state.copy(
        eventHandlers = state.eventHandlers + (eventClass -> handler.asInstanceOf[GameEvent => Unit])
      )

  override def getCurrentPhase: GamePhase = stateRef.get().currentPhase

  override def clearQueue(): Unit =
    updateState(_.copy(eventQueue = EventQueue.empty))

  private def updateState(f: EventHandlerState => EventHandlerState): Unit =
    var updated = false
    while !updated do
      val currentState = stateRef.get()
      val newState = f(currentState)
      updated = stateRef.compareAndSet(currentState, newState)

  private def processEvent(event: GameEvent): Unit =
    Try(handleEvent(event)) match
      case Success(_) => ()
      case Failure(exception) => exception.printStackTrace()

  private def handleEvent(event: GameEvent): Unit =
    val state = stateRef.get()

    event match
      case ShowMainMenu =>
        handleMenuTransition(MainMenu, Some(ViewState.MainMenu))
        Option.when(isGameActive)(stopEngine())
      case ShowGameView =>
        handleMenuTransition(Playing, Some(ViewState.GameView))
        Option.when(!engine.isRunning)(startEngine())
      case ShowInfoMenu =>
        handleMenuTransition(InfoMenu, Some(ViewState.InfoMenu))
      case ExitGame =>
        stopEngine().pipe(_ =>
          Platform.runLater(ViewController.cleanupBeforeExit())
          Thread.sleep(1000L)
          Platform.exit()
          sys.exit(0)
        )
      case Pause if state.currentPhase == Playing =>
        pauseEngine()
        handleMenuTransition(Paused, Some(ViewState.PauseMenu))
      case Resume if state.currentPhase == Paused =>
        resumeEngine()
        handleMenuTransition(Playing, Some(ViewState.GameView))
        ViewController.render()
      case GameWon =>
        pauseEngine()
        handleMenuTransition(Paused, Some(ViewState.Victory))
      case GameLost =>
        pauseEngine()
        handleMenuTransition(Paused, Some(ViewState.Defeat))
      case ContinueBattle =>
        engine.getController.foreach(_.handleContinueBattle())
        resumeEngine()
        handleMenuTransition(Playing, Some(ViewState.GameView))
      case NewGame =>
        stopEngine()
        engine.getController.foreach(_.handleNewGame())
        handleMenuTransition(Playing, Some(ViewState.GameView))
        Option.when(!engine.isRunning)(startEngine())
      case SelectWizard(wizardType) =>
        engine.getController.foreach(_.selectWizard(wizardType))
      case GridClicked(logicalPos, _, _) =>
        state.eventHandlers.get(classOf[GridClicked]).foreach(_(event))
      case _ =>
        state.eventHandlers.get(event.getClass).foreach(_(event))

  private def handleMenuTransition(newPhase: GamePhase, viewState: Option[ViewState]): Unit =
    updateState(_.copy(currentPhase = newPhase))
    engine.updatePhase(newPhase)
    viewState.foreach(ViewController.updateView)

  private def isGameActive: Boolean =
    stateRef.get().currentPhase match
      case Paused | Playing => true
      case _ => false

  private def startEngine(): Unit = engine.start()

  private def stopEngine(): Unit = engine.stop()

  private def pauseEngine(): Unit = engine.pause()

  private def resumeEngine(): Unit = engine.resume()

  private implicit class PipeOps[A](a: A):
    def pipe[B](f: A => B): B = f(a)

object EventHandler:
  def create(engine: GameEngine): EventHandler =
    new EventHandlerImpl(engine)

  def compose(handlers: (GameEvent => Unit)*): GameEvent => Unit =
    event => handlers.foreach(_(event))

  def conditional(predicate: GameEvent => Boolean)(handler: GameEvent => Unit): GameEvent => Unit =
    event => Option.when(predicate(event))(handler(event)).getOrElse(())