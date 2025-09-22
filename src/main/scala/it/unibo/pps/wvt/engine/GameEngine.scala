package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.controller.GameController

trait GameEngine {
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
}

class GameEngineImpl extends GameEngine {
  private var state: EngineState = EngineState()

  // Case class for internal state
  private case class EngineState(
                                  isRunning: Boolean = false,
                                  isPaused: Boolean = false,
                                  gameState: GameState = GameState.initial(),
                                  controller: Option[GameController] = None,
                                  gameLoop: Option[GameLoop] = None
                                ) {
    def withRunning(running: Boolean): EngineState = copy(isRunning = running)
    def withPaused(paused: Boolean): EngineState = copy(isPaused = paused)
    def withGameState(newState: GameState): EngineState = copy(gameState = newState)
    def withController(ctrl: GameController): EngineState = copy(controller = Some(ctrl))
    def withGameLoop(loop: GameLoop): EngineState = copy(gameLoop = Some(loop))
  }

  override def initialize(controller: GameController): Unit =
    state = state
      .withController(controller)
      .withGameLoop(GameLoop.create(this))
    println("Game Engine initialized")

  override def start(): Unit =
    if (!state.isRunning)
      state = state.withRunning(true)
      state.gameLoop.foreach(_.start())
      println("Game Engine started")

  override def stop(): Unit =
    if (state.isRunning)
      state = state.withRunning(false).withPaused(false)
      state.gameLoop.foreach(_.stop())
      println("Game Engine stopped")

  override def pause(): Unit =
    if (state.isRunning && !state.isPaused)
      state = state
        .withPaused(true)
        .withGameState(state.gameState.copy(isPaused = true))
      println("Game Engine paused")

  override def resume(): Unit =
    if (state.isRunning && state.isPaused)
      state = state
        .withPaused(false)
        .withGameState(state.gameState.copy(isPaused = false))
      println("Game Engine resumed")

  override def update(deltaTime: Long): Unit =
    state.controller.foreach(_.getEventHandler.processEvents())

    if (state.isRunning && !state.isPaused)
      state.controller.foreach(_.update())
      state = state.withGameState(state.gameState.updateTime(deltaTime))

  override def updatePhase(newPhase: GamePhase): Unit =
    state = state.withGameState(state.gameState.transitionTo(newPhase))

  override def isRunning: Boolean = state.isRunning
  override def isPaused: Boolean = state.isPaused
  override def currentState: GameState = state.gameState
}

object GameEngine {
  private var instance: Option[GameEngine] = None

  def create(controller: GameController): GameEngine =
    val engine = new GameEngineImpl()
    engine.initialize(controller)
    instance = Some(engine)
    engine

  def getInstance: Option[GameEngine] = instance
}