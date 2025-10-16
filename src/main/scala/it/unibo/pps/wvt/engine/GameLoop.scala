package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.engine.LoopStatus.*
import it.unibo.pps.wvt.utilities.GameConstants.*

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.util.Try

/** Represents the status of the game loop.
  *
  * - `Idle`: The game loop is not running.
  * - `Running`: The game loop is actively running.
  * - `Stopping`: The game loop is in the process of stopping.
  *
  * Provides utility methods to check the current status.
  */
sealed trait LoopStatus:
  def isRunning: Boolean = this match
    case Running => true
    case _       => false

/** Companion object for LoopStatus, containing case objects for each status. */
object LoopStatus:
  case object Idle     extends LoopStatus
  case object Running  extends LoopStatus
  case object Stopping extends LoopStatus

  /** Represents the state of the game loop, including timing and frame rate information.
    *
    * @param status      The current status of the game loop (Idle, Running, Stopping).
    * @param lastUpdate  The timestamp of the last update in nanoseconds.
    * @param accumulator The accumulated time in milliseconds for fixed time step processing.
    * @param frameCount  The number of frames processed in the current second.
    * @param fpsTimer    The timestamp used to calculate frames per second in milliseconds.
    * @param currentFps  The current frames per second.
    * @param isPaused    Indicates whether the game loop is currently paused.
    */
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

    /** Transitions the loop state to running, initializing timing variables.
      *
      * @return A new LoopState instance with status set to Running and timing variables initialized.
      */
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

    /** Transitions the loop state to idle, resetting timing variables.
      *
      * @return A new LoopState instance with status set to Idle and timing variables reset.
      */
    def stopRunning: LoopState =
      copy(status = LoopStatus.Idle, isPaused = false)

    /** Marks the loop state as paused, resetting the accumulator.
      *
      * @return A new LoopState instance with isPaused set to true and accumulator reset.
      */
    def markPaused(): LoopState =
      copy(
        isPaused = true,
        accumulator = 0L
      )

    /** Resumes the loop state from a paused state, updating timing variables.
      *
      * @param currentTime The current time in nanoseconds to set as lastUpdate.
      * @return A new LoopState instance with isPaused set to false and timing variables updated.
      */
    def resumeFromPause(currentTime: Long): LoopState =
      copy(
        lastUpdate = currentTime,
        accumulator = 0L,
        isPaused = false,
        fpsTimer = System.currentTimeMillis()
      )

    /** Updates the loop state with the current time, calculating frame time and updating the accumulator.
      *
      * @param currentTime The current time in nanoseconds.
      * @return A new LoopState instance with updated lastUpdate and accumulator.
      */
    def updateFrame(currentTime: Long): LoopState =
      val frameTime        = (currentTime - lastUpdate) / 1_000_000L
      val clampedFrameTime = frameTime.min(maxFrameTime)
      copy(
        lastUpdate = currentTime,
        accumulator = accumulator + clampedFrameTime
      )

    /** Consumes a specified time step from the accumulator.
      *
      * @param timeStep The time step in milliseconds to consume from the accumulator.
      * @return A new LoopState instance with the accumulator reduced by the time step.
      */
    def consumeTimeStep(timeStep: Long): LoopState =
      copy(accumulator = accumulator - timeStep)

    /** Increments the frame count by one.
      *
      * @return A new LoopState instance with the frame count incremented.
      */
    def incrementFrameCount: LoopState =
      copy(frameCount = frameCount + 1)

    /** Updates the frames per second (FPS) counter if one second has elapsed since the last update.
      *
      * @param currentTimeMs The current time in milliseconds.
      * @return A new LoopState instance with updated FPS information if one second has passed, otherwise returns the current state.
      */
    def updateFps(currentTimeMs: Long): LoopState =
      Option.when(currentTimeMs - fpsTimer >= 1000):
        copy(
          currentFps = frameCount,
          frameCount = 0,
          fpsTimer = currentTimeMs
        )
      .getOrElse(this)

    /** Checks if the game loop is currently running.
      *
      * @return True if the status is Running, false otherwise.
      */
    def isRunning: Boolean = status.isRunning

    /** Checks if the game loop is currently paused.
      *
      * @return True if the loop is paused, false otherwise.
      */
    def hasAccumulatedTime(timeStep: Long): Boolean = accumulator >= timeStep

/** Interface defining the game loop functionality.
  *
  * The GameLoop is responsible for managing the main game loop, including starting and stopping the loop,
  * checking if the loop is running, and retrieving the current frames per second (FPS).
  */
trait GameLoop:
  def start(): Unit
  def stop(): Unit
  def isRunning: Boolean
  def getCurrentFps: Int

/** Implementation of the GameLoop trait.
  *
  * This class manages the game loop using a scheduled executor service to run the loop at a fixed time step.
  * It handles starting and stopping the loop, updating the game state, processing accumulated frames,
  * and calculating the frames per second (FPS).
  *
  * @param engine The GameEngine instance that the game loop will update.
  */
class GameLoopImpl(engine: GameEngine) extends GameLoop:

  private val loopStateRef = new AtomicReference(LoopState())
  private val schedulerRef = new AtomicReference[Option[ScheduledExecutorService]](None)

  private val fixedTimeStep: Long = FRAME_TIME_MILLIS

  private type StateUpdater = LoopState => LoopState

  /** Atomically updates the loop state using the provided state updater function.
    *
    * @param f A function that takes the current LoopState and returns a new LoopState.
    */
  private def updateState(f: StateUpdater): Unit =
    @tailrec
    def loop(): Unit =
      val current  = loopStateRef.get()
      val newState = f(current)
      if !loopStateRef.compareAndSet(current, newState) then loop()
    loop()

  /** Reads the current loop state.
    *
    * @return The current LoopState.
    */
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

  /** Creates a single-threaded scheduled executor service with a custom thread factory.
    *
    * The thread is configured to be a daemon thread with maximum priority.
    *
    * @return A ScheduledExecutorService instance.
    */
  private def createExecutor: ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor: runnable =>
      val thread = new Thread(runnable, "GameLoop-Thread")
      thread.setDaemon(true)
      thread.setPriority(Thread.MAX_PRIORITY)
      thread

  /** Creates and starts the scheduler to run the game loop at a fixed rate.
    * The scheduler executes the game loop tick method at intervals defined by the fixed time step.
    */
  private def createAndStartScheduler(): Unit =
    val executor = createExecutor
    schedulerRef.set(Some(executor))

    executor.scheduleAtFixedRate(
      () => handleGameLoopTick(),
      0,
      fixedTimeStep,
      TimeUnit.MILLISECONDS
    )

  /** Handles a single tick of the game loop, catching and logging any exceptions that occur during execution.
    *
    * This method calls the main game loop tick method and recovers from any exceptions by printing the error message and stack trace.
    */
  private def handleGameLoopTick(): Unit =
    Try(gameLoopTick()).recover:
      case ex =>
        println(s"Exception in game loop: ${ex.getMessage}")
        ex.printStackTrace()

  /** The main game loop tick method.
    * This method updates the game state if the loop is running and the engine is not paused.
    * It processes accumulated frames and updates the FPS counter.
    */
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

  /** Recursively processes accumulated frames in the game loop.
    * This method checks if there is enough accumulated time to process a fixed time step
    * and if the engine is not paused. If both conditions are met, it updates the engine
    * and consumes the time step from the accumulator, then calls itself recursively.
    */
  @tailrec
  private def processAccumulatedFrames(): Unit =
    val state = readState
    if state.hasAccumulatedTime(fixedTimeStep) && !engine.isPaused then
      engine.update(fixedTimeStep)
      updateState(_.consumeTimeStep(fixedTimeStep))
      processAccumulatedFrames()

  /** Updates the FPS counter if the engine is not paused. */
  private def updateFpsCounter(): Unit =
    Option.when(!engine.isPaused):
      val currentTimeMs = System.currentTimeMillis()
      updateState: state =>
        state.incrementFrameCount.updateFps(currentTimeMs)
    .foreach(identity)

  /** Shuts down the scheduler gracefully. */
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

/** Companion object for GameLoop, providing a factory method to create a GameLoop instance. */
object GameLoop:

  /** Creates a new GameLoop instance with the provided GameEngine.
    *
    * @param engine The GameEngine instance that the game loop will update.
    * @return A new GameLoop instance.
    */
  def create(engine: GameEngine): GameLoop =
    new GameLoopImpl(engine)
