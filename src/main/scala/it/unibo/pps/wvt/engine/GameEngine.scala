package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.controller.GameController

trait GameEngine {
  def initialize(): Unit
  def start(): Unit
  def stop(): Unit
  def update(deltaTime: Long): Unit
  def isRunning: Boolean
  def currentState: GameState
}

// Main game engine implementation
class GameEngineImpl extends GameEngine {

  private var _isRunning: Boolean = false
  private var _gameState: GameState = GameState.initial()

  private var gameController: Option[GameController] = None

  private var gameLoop: Option[GameLoop] = None

  override def initialize(): Unit =
    gameController = Some(GameController(_gameState))
    gameLoop = Some(GameLoop.create(this))

  override def start(): Unit =
    if (!_isRunning)
      _isRunning = true
      gameLoop.foreach(_.start())
      println("Game Engine started")

  override def stop(): Unit =
    if (_isRunning)
      _isRunning = false
      gameLoop.foreach(_.stop())
      println("Game Engine stopped")

  override def update(deltaTime: Long): Unit =
    if(_isRunning)
      gameController.foreach { controller =>
        _gameState = controller.update(_gameState, deltaTime)
      }

      //TODO: Process input through controller

      //TODO: Trigger render through controller

  override def isRunning: Boolean = _isRunning

  override def currentState: GameState = _gameState
}

object GameEngine {
  def create(): GameEngine =
    val engine = new GameEngineImpl()
    engine.initialize()
    engine
}

