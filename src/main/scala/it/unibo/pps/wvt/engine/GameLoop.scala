package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.utilities.GameConstants.*

import java.util.concurrent.*

trait GameLoop {
  def start(): Unit
  def stop(): Unit
  def isRunning: Boolean
  def getCurrentFps: Int
}

class GameLoopImpl(engine: GameEngine) extends GameLoop {

  private var scheduler: Option[ScheduledExecutorService] = None
  private var running: Boolean = false

  // Time tracking
  private var lastUpdate: Long = 0L
  private var delta: Long = 0L

  // FPS tracking
  private var frameCount: Int = 0
  private var fpsTimer: Long = 0L
  private var currentFps: Int = 0


  override def start(): Unit =
    if (!running)
      running = true
      lastUpdate = System.nanoTime()
      fpsTimer = System.currentTimeMillis()

      val executor = Executors.newSingleThreadScheduledExecutor()
      scheduler = Some(executor)

      executor.scheduleAtFixedRate(
        () => gameLoopTick(),
        0,
        FRAME_TIME_MICROS,
        TimeUnit.MILLISECONDS
      )

      println(s"Game loop started with target FPS: $TARGET_FPS\n")

  override def stop(): Unit =
    if(running)
      running = false

      scheduler.foreach { exec =>
        exec.shutdown()
        try {
          if (!exec.awaitTermination(5, TimeUnit.SECONDS))
            exec.shutdownNow()
        } catch
          case _: InterruptedException =>
            exec.shutdownNow()
            Thread.currentThread().interrupt()
      }

      scheduler = None
      println("Game loop stopped")

  override def isRunning: Boolean = running

  override def getCurrentFps: Int = currentFps

  private def gameLoopTick(): Unit =
    if (running && engine.isRunning)
      // Calculate delta time
      val currentTime = System.nanoTime()
      delta = (currentTime - lastUpdate) / 1_000_000L // Convert to milliseconds
      lastUpdate = currentTime

      // Update the engine with delta time
      engine.update(delta)

      // Update FPS counter
      updateFpsCounter()

  private def updateFpsCounter(): Unit =
    frameCount += 1

    val currentTimeMs = System.currentTimeMillis()
    if (currentTimeMs - fpsTimer >= 1000)
      currentFps = frameCount
      frameCount = 0
      fpsTimer = currentTimeMs
}

object GameLoop {

  def create(engine: GameEngine): GameLoopImpl = new GameLoopImpl(engine)

}
