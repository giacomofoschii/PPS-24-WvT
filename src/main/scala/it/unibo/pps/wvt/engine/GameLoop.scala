package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.utilities.GameConstants.*
import java.util.concurrent.*
import scala.util.Try

trait GameLoop:
  def start(): Unit
  def stop(): Unit
  def isRunning: Boolean
  def getCurrentFps: Int

class GameLoopImpl(engine: GameEngine) extends GameLoop:

  private var scheduler: Option[ScheduledExecutorService] = None
  private var loopState: LoopState = LoopState()

  // Case class for immutable state
  private case class LoopState(
                                running: Boolean = false,
                                lastUpdate: Long = 0L,
                                accumulator: Long = 0L,
                                frameCount: Int = 0,
                                fpsTimer: Long = 0L,
                                currentFps: Int = 0
                              ):
    def startRunning: LoopState = copy(
      running = true,
      lastUpdate = System.nanoTime(),
      fpsTimer = System.currentTimeMillis(),
      accumulator = 0L
    )

    def stopRunning: LoopState = copy(running = false)

    def updateFrame(currentTime: Long): LoopState =
      val frameTime = (currentTime - lastUpdate) / 1_000_000L
      copy(
        lastUpdate = currentTime,
        accumulator = accumulator + frameTime
      )

    def consumeTimeStep(timeStep: Long): LoopState =
      copy(accumulator = accumulator - timeStep)

    def incrementFrameCount: LoopState = copy(frameCount = frameCount + 1)

    def updateFps(currentTimeMs: Long): LoopState =
      if currentTimeMs - fpsTimer >= 1000 then
        copy(
          currentFps = frameCount,
          frameCount = 0,
          fpsTimer = currentTimeMs
        )
      else this

  private val fixedTimeStep: Long = FRAME_TIME_MILLIS

  override def start(): Unit =
    if !loopState.running then
      loopState = loopState.startRunning

      val executor = Executors.newSingleThreadScheduledExecutor: r =>
        val thread = new Thread(r, "GameLoop-Thread")
        thread.setDaemon(true)
        thread.setPriority(Thread.MAX_PRIORITY)
        thread

      scheduler = Some(executor)

      executor.scheduleAtFixedRate(
        () => Try(gameLoopTick()).recover:
          case ex => println(s"Exception in game loop: ${ex.getMessage}"),
        0,
        fixedTimeStep,
        TimeUnit.MILLISECONDS
      )

  override def stop(): Unit =
    if loopState.running then
      loopState = loopState.stopRunning

      scheduler.foreach: exec =>
        exec.shutdown()
        Try:
          if !exec.awaitTermination(5, TimeUnit.SECONDS) then
            exec.shutdownNow()
        .recover:
          case _: InterruptedException =>
            exec.shutdownNow()
            Thread.currentThread().interrupt()

      scheduler = None

  override def isRunning: Boolean = loopState.running

  override def getCurrentFps: Int = loopState.currentFps

  private def gameLoopTick(): Unit =
    if loopState.running && engine.isRunning then
      val currentTime = System.nanoTime()
      loopState = loopState.updateFrame(currentTime)

      // Fixed time step update loop
      while loopState.accumulator >= fixedTimeStep do
        engine.update(fixedTimeStep)
        loopState = loopState.consumeTimeStep(fixedTimeStep)

      // Update FPS counter
      updateFpsCounter()

  private def updateFpsCounter(): Unit =
    loopState = loopState.incrementFrameCount
    val currentTimeMs = System.currentTimeMillis()
    loopState = loopState.updateFps(currentTimeMs)

object GameLoop:
  def create(engine: GameEngine): GameLoop = new GameLoopImpl(engine)

  // Utility functions for timing
  object TimingUtils:
    def calculateDelta(lastTime: Long, currentTime: Long): Long =
      (currentTime - lastTime) / 1_000_000L

    def interpolate(accumulator: Long, timeStep: Long): Double =
      accumulator.toDouble / timeStep.toDouble