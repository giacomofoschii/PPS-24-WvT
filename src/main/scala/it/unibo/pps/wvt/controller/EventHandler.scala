package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.controller.GameEvent.*
import it.unibo.pps.wvt.engine.{GamePhase, *}
import it.unibo.pps.wvt.engine.GamePhase.*
import it.unibo.pps.wvt.utilities.ViewConstants.DEBOUNCE_MS
import it.unibo.pps.wvt.view.{ViewController, ViewState}
import scalafx.application.Platform

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import scala.util.{Failure, Success, Try}

/** Represents the state of the EventHandler, including the event queue, registered handlers, and current game phase.
  *
  * @param eventQueue the queue of events to be processed.
  * @param eventHandlers a map of event classes to their corresponding handler functions.
  * @param currentPhase the current phase of the game.
  * @param lastEventTimes a map to track the last processed time for debounced events.
  */
case class EventHandlerState(
    eventQueue: EventQueue = EventQueue.empty,
    eventHandlers: Map[Class[_], GameEvent => Unit] = Map.empty,
    currentPhase: GamePhase = GamePhase.MainMenu,
    lastEventTimes: ConcurrentHashMap[String, Long] = new ConcurrentHashMap()
)

/** Interface defining the contract for an event handler. */
trait EventHandler:
  /** Posts a new event to the event queue.
    *
    * @param event the event to be posted.
    */
  def postEvent(event: GameEvent): Unit

  /** Processes all events in the queue and returns a list of processed events.
    *
    * @return a list of processed events.
    */
  def processEvents(): List[GameEvent]

  /** Registers a handler for a specific type of event.
    *
    * @param eventClass the class of the event to handle.
    * @param handler the function to handle the event.
    * @tparam T the type of the event.
    */
  def registerHandler[T <: GameEvent](eventClass: Class[T])(handler: T => Unit): Unit

  /** Gets the current phase of the game.
    *
    * @return the current game phase.
    */
  def getCurrentPhase: GamePhase

  /** Clears all events from the event queue. */
  def clearQueue(): Unit

/** Implementation of the EventHandler interface.
  *
  * @param engine
  *   The game engine instance to control game flow.
  */
class EventHandlerImpl(private val engine: GameEngine) extends EventHandler:

  private val stateRef = new AtomicReference(EventHandlerState())

  override def postEvent(event: GameEvent): Unit =
    updateState(_.copy(eventQueue = stateRef.get().eventQueue.enqueue(event)))

  override def processEvents(): List[GameEvent] =
    val currentState       = stateRef.get()
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

  /** Atomically updates the state of the event handler using the provided function.
    *
    * @param f the function to transform the current state.
    */
  private def updateState(f: EventHandlerState => EventHandlerState): Unit =
    var updated = false
    while !updated do
      val currentState = stateRef.get()
      val newState     = f(currentState)
      updated = stateRef.compareAndSet(currentState, newState)

  /** Processes a single event by invoking the appropriate handler and managing game state transitions.
    *
    * @param event the event to be processed.
    */
  private def processEvent(event: GameEvent): Unit =
    Try(handleEvent(event)) match
      case Success(_)         => ()
      case Failure(exception) => exception.printStackTrace()

  /** Handles specific game events and manages transitions between game phases.
    *
    * @param event the event to be handled.
    */
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
        Thread.sleep(50)
        handleMenuTransition(Paused, Some(ViewState.Victory))
      case GameLost =>
        pauseEngine()
        Thread.sleep(50)
        handleMenuTransition(Paused, Some(ViewState.Defeat))
      case ContinueBattle =>
        if !engine.isPaused then
          pauseEngine()
          Thread.sleep(50)

        engine.getController.foreach(_.handleContinueBattle())

        resumeEngine()
        handleMenuTransition(Playing, Some(ViewState.GameView))
        ViewController.render()
      case NewGame =>
        stopEngine()
        Thread.sleep(100)

        engine.getController.foreach(_.handleNewGame())
        handleMenuTransition(Playing, Some(ViewState.GameView))

        Option.when(!engine.isRunning)(startEngine())
      case SelectWizard(wizardType) =>
        engine.getController.foreach(_.selectWizard(wizardType))
      case GridClicked(logicalPos, _, _) =>
        state.eventHandlers.get(classOf[GridClicked]).foreach(_(event))
      case _ =>
        state.eventHandlers.get(event.getClass).foreach(_(event))

  /** Handles transitions between different game phases and updates the view state accordingly.
    *
    * @param newPhase the new game phase to transition to.
    * @param viewState an optional view state to update the UI.
    */
  private def handleMenuTransition(newPhase: GamePhase, viewState: Option[ViewState]): Unit =
    updateState(_.copy(currentPhase = newPhase))
    engine.updatePhase(newPhase)
    viewState.foreach(ViewController.updateView)

  /** Determines if the given event should be debounced to prevent rapid repeated processing.
    *
    * @param event the event to check for debouncing.
    * @return true if the event should be debounced, false otherwise.
    */
  private def shouldDebounce(event: GameEvent): Boolean =
    val debounceKey = event match
      case ContinueBattle  => "ContinueBattle"
      case NewGame         => "NewGame"
      case SelectWizard(_) => "SelectWizard"
      case _               => return false

    val currentTime        = System.currentTimeMillis()
    val lastTime           = Option(stateRef.get().lastEventTimes.get(debounceKey)).getOrElse(0L)
    val timeSinceLastEvent = currentTime - lastTime

    if timeSinceLastEvent < DEBOUNCE_MS then
      true
    else
      stateRef.get().lastEventTimes.put(debounceKey, currentTime)
      false

  /** Checks if the given game phase is a menu phase.
    *
    * @return true if the phase is a menu phase, false otherwise.
    */
  private def isGameActive: Boolean =
    stateRef.get().currentPhase match
      case Paused | Playing => true
      case _                => false

  /** Starts the game engine. */
  private def startEngine(): Unit = engine.start()

  /** Stops the game engine. */
  private def stopEngine(): Unit = engine.stop()

  /** Pauses the game engine. */
  private def pauseEngine(): Unit = engine.pause()

  /** Resumes the game engine. */
  private def resumeEngine(): Unit = engine.resume()

  /** Implicit class to add a pipe method for function application.
    *
    * @param a the value to be transformed.
    * @tparam A the type of the value.
    */
  private implicit class PipeOps[A](a: A):
    def pipe[B](f: A => B): B = f(a)

/** Companion object for the EventHandler, providing factory methods and utility functions. */
object EventHandler:
  /** Creates a new EventHandler instance associated with the given game engine.
    *
    * @param engine the game engine to associate with the event handler.
    * @return a new EventHandler instance.
    */
  def create(engine: GameEngine): EventHandler =
    EventHandlerImpl(engine)

  /** Composes multiple event handlers into a single handler that invokes each in sequence.
    *
    * @param handlers the event handlers to compose.
    * @return a composed event handler.
    */
  def compose(handlers: (GameEvent => Unit)*): GameEvent => Unit =
    event => handlers.foreach(_(event))

  /** Creates a conditional event handler that invokes the given handler only if the predicate is satisfied.
    *
    * @param predicate the condition to check before invoking the handler.
    * @param handler the event handler to invoke if the predicate is true.
    * @return a conditional event handler.
    */
  def conditional(predicate: GameEvent => Boolean)(handler: GameEvent => Unit): GameEvent => Unit =
    event => Option.when(predicate(event))(handler(event)).getOrElse(())
