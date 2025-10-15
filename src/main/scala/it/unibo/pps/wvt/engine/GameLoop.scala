package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.engine.LoopStatus.*
import it.unibo.pps.wvt.utilities.GameConstants.*

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.util.Try

sealed trait LoopStatus:
  def isRunning: Boolean = this match
    case Running => true
    case _       => false

object LoopStatus:
  private case object Idle extends LoopStatus
  case object Running      extends LoopStatus
  case object Stopping     extends LoopStatus

  case class LoopState(
      status: LoopStatus = Idle,
      lastUpdate: Long = 0L,
      accumulator: Long = 0L,
      frameCount: Int = 0,
      fpsTimer: Long = 0L,
      currentFps: Int = 0,
      isPaused: Boolean = false
  ):

    private val maxFrameTime: Long = 33L

    def startRunning: LoopState =
      val currentTime   = System.nanoTime()
      val currentTimeMs = System.currentTimeMillis()
      copy(
        status = LoopStatus.Running,
        lastUpdate = currentTime,
        fpsTimer = currentTimeMs,
        accumulator = 0L,
        frameCount = 0,
        isPaused = false
      )

    def stopRunning: LoopState =
      copy(status = LoopStatus.Idle, isPaused = false)

    def markPaused(): LoopState =
      copy(
        isPaused = true,
        accumulator = 0L
      )

    def resumeFromPause(currentTime: Long): LoopState =
      copy(
        lastUpdate = currentTime,
        accumulator = 0L,
        isPaused = false,
        fpsTimer = System.currentTimeMillis()
      )

    def updateFrame(currentTime: Long): LoopState =
      val frameTime        = (currentTime - lastUpdate) / 1_000_000L
      val clampedFrameTime = frameTime.min(maxFrameTime)
      copy(
        lastUpdate = currentTime,
        accumulator = accumulator + clampedFrameTime
      )

    def consumeTimeStep(timeStep: Long): LoopState =
      copy(accumulator = accumulator - timeStep)

    def incrementFrameCount: LoopState =
      copy(frameCount = frameCount + 1)

    def updateFps(currentTimeMs: Long): LoopState =
      Option.when(currentTimeMs - fpsTimer >= 1000):
        copy(
          currentFps = frameCount,
          frameCount = 0,
          fpsTimer = currentTimeMs
        )
      .getOrElse(this)

    def isRunning: Boolean                          = status.isRunning
    def hasAccumulatedTime(timeStep: Long): Boolean = accumulator >= timeStep

trait GameLoop:
  def start(): Unit
  def stop(): Unit
  def isRunning: Boolean
  def getCurrentFps: Int

class GameLoopImpl(engine: GameEngine) extends GameLoop:

  private val loopStateRef = new AtomicReference(LoopState())
  private val schedulerRef = new AtomicReference[Option[ScheduledExecutorService]](None)

  private val fixedTimeStep: Long = FRAME_TIME_MILLIS

  private type StateUpdater = LoopState => LoopState

  private def updateState(f: StateUpdater): Unit =
    @tailrec
    def loop(): Unit =
      val current  = loopStateRef.get()
      val newState = f(current)
      if !loopStateRef.compareAndSet(current, newState) then loop()
    loop()

  private def readState: LoopState = loopStateRef.get()

  override def start(): Unit =
    Option.when(!readState.isRunning)(())
      .foreach: _ =>
        updateState(_.startRunning)
        createAndStartScheduler()

  override def stop(): Unit =
    Option.when(readState.isRunning)(())
      .foreach: _ =>
        updateState(_.stopRunning)
        shutdownScheduler()

  override def isRunning: Boolean = readState.isRunning

  override def getCurrentFps: Int = readState.currentFps

  private def createExecutor: ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor: runnable =>
      val thread = new Thread(runnable, "GameLoop-Thread")
      thread.setDaemon(true)
      thread.setPriority(Thread.MAX_PRIORITY)
      thread

  private def createAndStartScheduler(): Unit =
    val executor = createExecutor
    schedulerRef.set(Some(executor))

    executor.scheduleAtFixedRate(
      () => handleGameLoopTick(),
      0,
      fixedTimeStep,
      TimeUnit.MILLISECONDS
    )

  private def handleGameLoopTick(): Unit =
    Try(gameLoopTick()).recover:
      case ex =>
        println(s"Exception in game loop: ${ex.getMessage}")
        ex.printStackTrace()

  private def gameLoopTick(): Unit =
    Option(readState)
      .filter(_.isRunning)
      .filter(_ => engine.isRunning)
      .foreach: _ =>
        if engine.isPaused then
          updateState(_.markPaused())
        else
          val currentTime = System.nanoTime()
          Option(readState)
            .filter(_.isPaused)
            .foreach(_ => updateState(_.resumeFromPause(currentTime)))
          Option(readState)
            .filterNot(_.isPaused)
            .foreach: _ =>
              updateState(_.updateFrame(currentTime))
              processAccumulatedFrames()
              updateFpsCounter()

  @tailrec
  private def processAccumulatedFrames(): Unit =
    val state = readState
    if state.hasAccumulatedTime(fixedTimeStep) && !engine.isPaused then
      engine.update(fixedTimeStep)
      updateState(_.consumeTimeStep(fixedTimeStep))
      processAccumulatedFrames()

  private def updateFpsCounter(): Unit =
    Option.when(!engine.isPaused):
      val currentTimeMs = System.currentTimeMillis()
      updateState: state =>
        state.incrementFrameCount.updateFps(currentTimeMs)
    .foreach(identity)

  private def shutdownScheduler(): Unit =
    schedulerRef.get()
      .foreach: executor =>
        executor.shutdown()
        Try:
          Option.when(!executor.awaitTermination(5, TimeUnit.SECONDS))(())
            .foreach(_ => executor.shutdownNow())
        .recover:
          case _: InterruptedException =>
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        schedulerRef.set(None)

object GameLoop:
  def create(engine: GameEngine): GameLoop =
    new GameLoopImpl(engine)
