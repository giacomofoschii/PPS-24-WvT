package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.model._
import it.unibo.pps.wvt.utilities.GameConstants._

import java.util.concurrent._
import scala.util.{Failure, Success, Try}

trait GameLoop {
  self: GameLoopConfig =>

  def start(): Unit
  def stop(): Unit
  def isRunning: Boolean

  protected def tick(delta: Long): Unit
}

trait GameLoopConfig {
  def targetFps: Int = TARGET_FPS
  def tickRate: Long = 1000 / targetFps
}

trait GameLoopStats {
  self: GameLoop =>

  private var frameCount: Long = 0
  private var totalTime: Long = 0
  private var lastFpsTime: Long = System.currentTimeMillis()
  private var currentFps: Long = 0

  protected def updateStats(delta: Long): Unit =
    frameCount += 1
    totalTime += delta

    val currentTime = System.currentTimeMillis()
    if (currentTime - lastFpsTime >= 1000)
      currentFps = frameCount * 1000 / (currentTime - lastFpsTime)
      frameCount = 0
      lastFpsTime = currentTime
}

class EventBasedGameLoop(engine: GameEngine)
  extends GameLoop
  with GameLoopConfig
  with GameLoopStats {

  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(THREAD_NUM)
  private var running: Boolean = false
  private var lastUpdate: Long = System.nanoTime()

  private val gameLoopTask: Runnable = () =>
    if (running && engine.isRunning)
      val currentTime = System.nanoTime()
      val delta = (currentTime - lastUpdate) / 1_000_000
      lastUpdate = currentTime

      tick(delta)
      updateStats(delta)
    else if (!engine.isRunning && running)
      // Stop the loop if the engine is no longer running
      stop()

  override def start(): Unit =
    if (!running)
      running = true
      lastUpdate = System.nanoTime()

      scheduler.scheduleAtFixedRate(
        gameLoopTask,
        0,
        tickRate,
        TimeUnit.MILLISECONDS
      )

      println(s"Game loop started with target FPS: $targetFps and tick rate: $tickRate ms\n")

  override def stop(): Unit =
    if(running)
      running = false
      scheduler.shutdown()

      try
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
          scheduler.shutdownNow()
      catch
        case _: InterruptedException =>
          scheduler.shutdownNow()
          Thread.currentThread().interrupt()

  override def isRunning: Boolean = running

  override protected def tick(delta: Long): Unit =
    Try(engine.update(delta)) match
        case Failure(ex) =>
          println(s"Error in game loop: ${ex.getMessage}")
          ex.printStackTrace()
        case Success(_) => // Continue normally
}

class GameController(engineFactory: () => GameEngine = () => GameEngine.create()) {

  private var engine: GameEngine = engineFactory()
  private var gameLoop: Option[GameLoop] = None

  def startNewGame(): Unit =
    // Stop existing game if running
    stopGame()

    // Create new engine and loop
    engine = engineFactory()
    val loop = new EventBasedGameLoop(engine)
    gameLoop = Some(loop)

    engine.processEvent(GameEvent.StartWave)
    loop.start()

  def pauseGame(): Unit =
    engine.processEvent(GameEvent.PauseGame)

  def resumeGame(): Unit =
    engine.processEvent(GameEvent.ResumeGame)

  def stopGame(): Unit =
    gameLoop.foreach(_.stop())
    gameLoop = None
    engine.processEvent(GameEvent.EndGame)

  def placeWizard(wiz: Wizard, pos: Position): Unit =
    engine.processEvent(GameEvent.PlaceWizard(wiz, pos))

  def getGameState: GameState = engine.currentState

  def isRunning: Boolean = gameLoop.exists(_.isRunning)

  def continueToNextWave(): Unit =
    if(engine.currentState.phase == GamePhase.WaveComplete)
      engine.processEvent(GameEvent.StartWave)

  def exitGame(): Unit =
    stopGame()
}

object GameLoop {

  def create(engine: GameEngine): EventBasedGameLoop =
    new EventBasedGameLoop(engine)

  def withCustomFPS(engine: GameEngine, fps: Int): EventBasedGameLoop =
    new EventBasedGameLoop(engine) {
      override def targetFps: Int = fps
      override def tickRate: Long = 1000 / targetFps
    }

}
