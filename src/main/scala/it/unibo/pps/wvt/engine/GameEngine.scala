package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.controller.GameController
import it.unibo.pps.wvt.engine.EngineStatus.*

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

/** Represents the status of the game engine.
  *
  * - Stopped: The engine is not running.
  * - Running: The engine is actively running and updating the game state.
  * - Paused: The engine is temporarily paused, halting game updates but retaining the current state.
  */
sealed trait EngineStatus:
  def isRunning: Boolean = this match
    case EngineStatus.Running => true
    case _                    => false

  def isPaused: Boolean = this match
    case EngineStatus.Paused => true
    case _                   => false

/** Companion object for EngineStatus, providing case objects for each status. */
object EngineStatus:
  case object Stopped extends EngineStatus
  case object Running extends EngineStatus
  case object Paused  extends EngineStatus

/** Represents the current state of the game engine, including its status, game state, controller, and game loop.
  *
  * @param status The current status of the engine (Stopped, Running, Paused).
  * @param gameState The current state of the game, including time and phase.
  * @param controller An optional game controller managing game logic and events.
  * @param gameLoop An optional game loop responsible for periodic updates.
  */
case class EngineState(
    status: EngineStatus = EngineStatus.Stopped,
    gameState: GameState = GameState.initial(),
    controller: Option[GameController] = None,
    gameLoop: Option[GameLoop] = None
):
  /** Returns a new EngineState with the updated status.
    *
    * @param newStatus The new status to set for the engine.
    * @return A new EngineState instance with the updated status.
    */
  def withStatus(newStatus: EngineStatus): EngineState =
    copy(status = newStatus)

  /** Returns a new EngineState with the updated game state.
    *
    * @param newState The new game state to set.
    * @return A new EngineState instance with the updated game state.
    */
  def withGameState(newState: GameState): EngineState =
    copy(gameState = newState)

  /** Returns a new EngineState with the provided game controller.
    *
    * @param ctrl The game controller to set.
    * @return A new EngineState instance with the updated controller.
    */
  def withController(ctrl: GameController): EngineState =
    copy(controller = Some(ctrl))

  /** Returns a new EngineState with the provided game loop.
    *
    * @param loop The game loop to set.
    * @return A new EngineState instance with the updated game loop.
    */
  def withGameLoop(loop: GameLoop): EngineState =
    copy(gameLoop = Some(loop))

  /** Transitions the engine to a new status, updating the game state accordingly.
    *
    * @param newStatus The new status to transition to.
    * @return A new EngineState instance with the updated status and game state.
    */
  def transitionTo(newStatus: EngineStatus): EngineState = newStatus match
    case Running =>
      copy(
        status = Running,
        gameState = gameState.copy(isPaused = false)
      )
    case Paused =>
      copy(
        status = Paused,
        gameState = gameState.copy(isPaused = true)
      )
    case Stopped =>
      copy(
        status = Stopped,
        gameState = gameState.copy(isPaused = false)
      )

  /** Updates the game time if the engine is running.
    *
    * @param deltaTime The time increment to add to the game time.
    * @return A new EngineState instance with the updated game time if running, otherwise the same state.
    */
  def updateGameTime(deltaTime: Long): EngineState = status match
    case Running =>
      copy(gameState = gameState.updateTime(deltaTime))
    case _ => this

  /** Updates the current game phase.
    *
    * @param newPhase The new game phase to set.
    * @return A new EngineState instance with the updated game phase.
    */
  def updatePhase(newPhase: GamePhase): EngineState =
    copy(gameState = gameState.transitionTo(newPhase))

  /** Checks if the engine is currently running.
    *
    * @return True if the engine is running, false otherwise.
    */
  def isRunning: Boolean = status.isRunning

  /** Checks if the engine is currently paused.
    *
    * @return True if the engine is paused, false otherwise.
    */
  def isPaused: Boolean = status.isPaused

  /** Checks if the engine is currently stopped.
    *
    * @return True if the engine is stopped, false otherwise.
    */
  def isStopped: Boolean = status == Stopped

  def canStart: Boolean  = !isRunning
  def canStop: Boolean   = isRunning
  def canPause: Boolean  = isRunning && !isPaused
  def canResume: Boolean = isRunning && isPaused
  def canUpdate: Boolean = isRunning && !isPaused

/** Trait defining the interface for a game engine, including methods for initialization, control, and state management. */
trait GameEngine:

  /** Initializes the game engine with the provided game controller.
    *
    * @param controller The game controller to manage game logic and events.
    */
  def initialize(controller: GameController): Unit

  /** Starts the game engine, transitioning it to the running state and beginning periodic updates. */
  def start(): Unit

  /** Stops the game engine, transitioning it to the stopped state and halting updates. */
  def stop(): Unit

  /** Pauses the game engine, halting updates but retaining the current state. */
  def pause(): Unit

  /** Resumes the game engine from a paused state, continuing updates. */
  def resume(): Unit

  /** Updates the game engine with the specified time increment, processing events and updating game logic.
    *
    * @param deltaTime The time increment to add to the game time.
    */
  def update(deltaTime: Long): Unit

  /** Checks if the game engine is currently running.
    *
    * @return True if the engine is running, false otherwise.
    */
  def isRunning: Boolean

  /** Checks if the game engine is currently paused.
    *
    * @return True if the engine is paused, false otherwise.
    */
  def isPaused: Boolean

  /** Retrieves the current state of the game, including time and phase.
    *
    * @return The current GameState instance.
    */
  def currentState: GameState

  /** Updates the current game phase.
    *
    * @param phase The new game phase to set.
    */
  def updatePhase(phase: GamePhase): Unit

  /** Retrieves the optional game controller managing game logic and events.
    *
    * @return An Option containing the GameController if set, or None otherwise.
    */
  def getController: Option[GameController]

/** Concrete implementation of the GameEngine trait, managing the engine state and providing thread-safe operations. */
class GameEngineImpl extends GameEngine:

  private val EngineStateRef = new AtomicReference(EngineState())
  private type EngineStateUpdater = EngineState => EngineState

  /** Atomically updates the engine state using the provided updater function.
    *
    * @param f A function that takes the current EngineState and returns a new EngineState.
    */
  private def updateState(f: EngineStateUpdater): Unit =
    @tailrec
    def loop(): Unit =
      val current  = EngineStateRef.get()
      val newState = f(current)
      if !EngineStateRef.compareAndSet(current, newState) then loop()
    loop()

  /** Retrieves the current engine state.
    *
    * @return The current EngineState instance.
    */
  private def readState: EngineState = EngineStateRef.get()

  override def initialize(ctrl: GameController): Unit =
    updateState: state =>
      state
        .withController(ctrl)
        .withGameLoop(GameLoop.create(this))

  override def start(): Unit =
    val currentState = readState
    Option.when(!currentState.isRunning)(())
      .foreach: _ =>
        updateState(_.transitionTo(EngineStatus.Running))
        val updatedState = readState
        updatedState.gameLoop.foreach(_.start())

  override def stop(): Unit =
    val currentState = readState
    Option.when(currentState.isRunning)(())
      .foreach: _ =>
        currentState.gameLoop.foreach(_.stop())
        updateState(_.transitionTo(EngineStatus.Stopped))

  override def pause(): Unit =
    val state = readState
    Option.when(state.status == EngineStatus.Running)(())
      .foreach: _ =>
        updateState(_.transitionTo(EngineStatus.Paused))

  override def resume(): Unit =
    val state = readState
    Option.when(state.status == EngineStatus.Paused)(())
      .foreach: _ =>
        updateState(_.transitionTo(EngineStatus.Running))

  override def update(deltaTime: Long): Unit =
    val state = readState

    state.controller.foreach(_.getEventHandler.processEvents())

    Option(state)
      .filter(_.status == EngineStatus.Running)
      .foreach: runningState =>
        runningState.controller.foreach(_.update())
        updateState(_.updateGameTime(deltaTime))

  override def updatePhase(newPhase: GamePhase): Unit =
    updateState(_.updatePhase(newPhase))

  override def isRunning: Boolean                    = readState.isRunning
  override def isPaused: Boolean                     = readState.isPaused
  override def currentState: GameState               = readState.gameState
  override def getController: Option[GameController] = readState.controller

/** Singleton object managing the single instance of the GameEngine.
  * Provides methods to create and retrieve the engine instance.
  */
object GameEngine:
  private var instance: Option[GameEngine] = None

  /** Creates and initializes the game engine with the provided controller if not already created.
    *
    * @param controller The game controller to manage game logic and events.
    * @return The singleton instance of the GameEngine.
    */
  def create(controller: GameController): GameEngine =
    val engine = new GameEngineImpl()
    engine.initialize(controller)
    instance = Some(engine)
    engine

  /** Retrieves the singleton instance of the GameEngine if it has been created.
    *
    * @return An Option containing the GameEngine instance if created, or None otherwise.
    */
  def getInstance: Option[GameEngine] = instance
