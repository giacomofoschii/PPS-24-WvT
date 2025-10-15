package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.controller.GameController
import it.unibo.pps.wvt.engine.EngineStatus.*

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

sealed trait EngineStatus:
  def isRunning: Boolean = this match
    case EngineStatus.Running => true
    case _                    => false

  def isPaused: Boolean = this match
    case EngineStatus.Paused => true
    case _                   => false

object EngineStatus:
  case object Stopped extends EngineStatus
  case object Running extends EngineStatus
  case object Paused  extends EngineStatus

case class EngineState(
    status: EngineStatus = EngineStatus.Stopped,
    gameState: GameState = GameState.initial(),
    controller: Option[GameController] = None,
    gameLoop: Option[GameLoop] = None
):
  def withStatus(newStatus: EngineStatus): EngineState =
    copy(status = newStatus)

  def withGameState(newState: GameState): EngineState =
    copy(gameState = newState)

  def withController(ctrl: GameController): EngineState =
    copy(controller = Some(ctrl))

  def withGameLoop(loop: GameLoop): EngineState =
    copy(gameLoop = Some(loop))

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

  def updateGameTime(deltaTime: Long): EngineState = status match
    case Running =>
      copy(gameState = gameState.updateTime(deltaTime))
    case _ => this

  def updatePhase(newPhase: GamePhase): EngineState =
    copy(gameState = gameState.transitionTo(newPhase))

  def isRunning: Boolean = status.isRunning
  def isPaused: Boolean  = status.isPaused
  def isStopped: Boolean = status == Stopped

  def canStart: Boolean  = !isRunning
  def canStop: Boolean   = isRunning
  def canPause: Boolean  = isRunning && !isPaused
  def canResume: Boolean = isRunning && isPaused
  def canUpdate: Boolean = isRunning && !isPaused

trait GameEngine:
  def initialize(controller: GameController): Unit
  def start(): Unit
  def stop(): Unit
  def pause(): Unit
  def resume(): Unit
  def update(deltaTime: Long): Unit
  def isRunning: Boolean
  def isPaused: Boolean
  def currentState: GameState
  def updatePhase(phase: GamePhase): Unit
  def getController: Option[GameController]

class GameEngineImpl extends GameEngine:

  private val EngineStateRef = new AtomicReference(EngineState())
  private type EngineStateUpdater = EngineState => EngineState

  private def updateState(f: EngineStateUpdater): Unit =
    @tailrec
    def loop(): Unit =
      val current  = EngineStateRef.get()
      val newState = f(current)
      if !EngineStateRef.compareAndSet(current, newState) then loop()
    loop()

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

object GameEngine:
  private var instance: Option[GameEngine] = None

  def create(controller: GameController): GameEngine =
    val engine = new GameEngineImpl()
    engine.initialize(controller)
    instance = Some(engine)
    engine

  def getInstance: Option[GameEngine] = instance
