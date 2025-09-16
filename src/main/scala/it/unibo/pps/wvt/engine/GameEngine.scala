package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.controller.GameController

trait GameEngine {
  def initialize(): Unit
  def start(): Unit
  def stop(): Unit
  def update(deltaTime: Long): Unit
  def isRunning: Boolean
  def currentState: GameState
  def processEvent(event: GameEvent): Unit
}

// Main game engine implementation
class GameEngineImpl extends GameEngine {

  private var _isRunning: Boolean = false
  private var _gameState: GameState = GameState.initial()

  private var gameController: Option[GameController] = None
  private var gameLoop: Option[GameLoop] = None
  private val eventProcessor: EventProcessor = new EventProcessor()

  override def initialize(): Unit =
    gameController = Some(GameController(_gameState))
    gameLoop = Some(GameLoop.create(this))
    registerEventHandlers()

  private def registerEventHandlers(): Unit =
    // Core engine events
    eventProcessor.registerHandler(classOf[GameEvent.Initialize.type],
      _ => {
        _gameState = GameState.initial()
        gameController = Some(new GameController(_gameState))
      })

    eventProcessor.registerHandler(classOf[GameEvent.Start.type],
      _ => start())

    eventProcessor.registerHandler(classOf[GameEvent.Stop.type],
      _ => stop())

    eventProcessor.registerHandler(classOf[GameEvent.Pause.type],
      _ => {
        if (_gameState.phase == GamePhase.Playing)
          _gameState = _gameState.copy(isPaused = true, phase = GamePhase.Paused)
      })

    eventProcessor.registerHandler(classOf[GameEvent.Resume.type],
      _ => {
        if (_gameState.phase == GamePhase.Paused)
          _gameState = _gameState.copy(isPaused = false, phase = GamePhase.Playing)
      })

    // Menu navigation events
    eventProcessor.registerHandler(classOf[GameEvent.ShowMainMenu.type],
      _ => {
        _gameState = _gameState.copy(phase = GamePhase.MainMenu)
      })

    eventProcessor.registerHandler(classOf[GameEvent.ShowGameView.type],
      _ => {
        _gameState = _gameState.copy(phase = GamePhase.Playing)
      })


    eventProcessor.registerHandler(classOf[GameEvent.ShowInfoMenu.type],
      _ => {
        _gameState = _gameState.copy(phase = GamePhase.InfoMenu)
      })

    eventProcessor.registerHandler(classOf[GameEvent.ExitGame.type],
      _ => {
          _gameState = _gameState.copy(phase = GamePhase.GameOver)
          stop()
      })

    // Update event
    eventProcessor.registerHandler(classOf[GameEvent.Update],
      event => {
        val updateEvent = event.asInstanceOf[GameEvent.Update]
        gameController.foreach { controller =>
          _gameState = controller.update(_gameState, updateEvent.deltaTime)
        }
      })

    // Render event
    eventProcessor.registerHandler(classOf[GameEvent.Render.type],
    _ => {
      // Render event processed - view will handle actual rendering
      // This is just the trigger from the game loop
    })


  override def start(): Unit =
    if (!_isRunning)
      _isRunning = true
      gameLoop.foreach(_.start())
      println("Game Engine started")

  override def stop(): Unit =
    if (_isRunning)
      _isRunning = false
      gameLoop.foreach(_.stop())
      eventProcessor.clearQueue()
      println("Game Engine stopped")

  override def update(deltaTime: Long): Unit =
    if(_isRunning)
      eventProcessor.processEvents()
      if (!_gameState.isPaused)
        gameController.foreach { controller =>
          _gameState = controller.update(_gameState, deltaTime)
        }

      gameLoop.foreach { loop =>
        _gameState = _gameState.copy(fps = loop.getCurrentFps)
      }

      //Trigger render event (even when paused to show pause state)
      eventProcessor.postEvent(GameEvent.Render)

  override def isRunning: Boolean = _isRunning

  override def currentState: GameState = _gameState

  override def processEvent(event: GameEvent): Unit =
    eventProcessor.postEvent(event)
}

object GameEngine {
  private var instance: Option[GameEngine] = None

  def create(): GameEngine =
    val engine = new GameEngineImpl()
    engine.initialize()
    instance = Some(engine)
    engine

  def getInstance: Option[GameEngine] = instance
}

