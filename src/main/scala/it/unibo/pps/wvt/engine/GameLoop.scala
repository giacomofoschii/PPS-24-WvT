package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.input.InputSystem
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
  private var acc = 0L

  // FPS tracking
  private var frameCount: Int = 0
  private var fpsTimer: Long = 0L
  private var currentFps: Int = 0

  // Fixed time step for consistent updates
  private val fixedTimeStep: Long = FRAME_TIME_MICROS

  override def start(): Unit =
    if (!running)
      running = true
      lastUpdate = System.nanoTime()
      fpsTimer = System.currentTimeMillis()
      acc = 0L

      val executor = Executors.newSingleThreadScheduledExecutor(r => {
        val thread = new Thread(r, "GameLoop-Thread")
        thread.setDaemon(true)
        thread.setPriority(Thread.MAX_PRIORITY)
        thread
      })

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
      try
        // Calculate delta time
        val currentTime = System.nanoTime()
        val frameTime = (currentTime - lastUpdate) / 1_000_000L // Convert to milliseconds
        lastUpdate = currentTime
        acc += frameTime

        while(acc >= fixedTimeStep)
          // Update game state with fixed time step
          engine.update(fixedTimeStep)
          acc -= fixedTimeStep

        // Update FPS counter
        updateFpsCounter()

      catch
        case ex: Exception =>
          println(s"Exception in game loop: ${ex.getMessage}")
          ex.printStackTrace()

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
