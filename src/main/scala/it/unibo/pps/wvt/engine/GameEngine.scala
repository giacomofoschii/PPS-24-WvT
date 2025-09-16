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
}

// Minimal game engine - only core lifecycle
class GameEngineImpl extends GameEngine {

  private var _isRunning: Boolean = false
  private var _isPaused: Boolean = false
  private var _gameState: GameState = GameState.initial()

  private var gameController: Option[GameController] = None
  private var gameLoop: Option[GameLoop] = None

  override def initialize(controller: GameController): Unit = {
    gameController = Some(controller)
    gameLoop = Some(GameLoop.create(this))
    println("Game Engine initialized")
  }

  override def start(): Unit = {
    if (!_isRunning) {
      _isRunning = true
      gameLoop.foreach(_.start())
      println("Game Engine started")
    }
  }

  override def stop(): Unit = {
    if (_isRunning) {
      _isRunning = false
      _isPaused = false
      gameLoop.foreach(_.stop())
      println("Game Engine stopped")
    }
  }

  override def pause(): Unit = {
    if (_isRunning && !_isPaused) {
      _isPaused = true
      _gameState = _gameState.copy(isPaused = true)
      println("Game Engine paused")
    }
  }

  override def resume(): Unit = {
    if (_isRunning && _isPaused) {
      _isPaused = false
      _gameState = _gameState.copy(isPaused = false)
      println("Game Engine resumed")
    }
  }

  override def update(deltaTime: Long): Unit = {
    if (_isRunning && !_isPaused) {
      // Update game state
      gameController.foreach { controller =>
        _gameState = controller.update(_gameState, deltaTime)
      }

      // Update FPS
      gameLoop.foreach { loop =>
        _gameState = _gameState.copy(fps = loop.getCurrentFps)
      }
    }
  }

  override def isRunning: Boolean = _isRunning
  override def isPaused: Boolean = _isPaused
  override def currentState: GameState = _gameState
}

object GameEngine {
  private var instance: Option[GameEngine] = None

  def create(controller: GameController): GameEngine = {
    val engine = new GameEngineImpl()
    engine.initialize(controller)
    instance = Some(engine)
    engine
  }

  def getInstance: Option[GameEngine] = instance
}