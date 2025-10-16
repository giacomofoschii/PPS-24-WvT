package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.utilities.GridMapper.LogicalCoords

import scala.collection.immutable.Queue

/** Represents a game event with a timestamp and priority. */
trait GameEvent:

  /** Priority of the event, lower values indicate higher priority.
    * System events have the highest priority (0), followed by game state events (1),
    * menu events (2), and input events (3).
    *
    * @return the priority of the event
    */
  def priority: Int

/** Companion object for GameEvent containing various event case classes and objects.
  * Possible game event cases: Initialize, Start, Stop Pause Resume, ShowMainMenu, ShowGameView, ShowInfoMenu,
  * ExitGame, GameWon, GameLost, ContinueBattle, NewGame, GridClicked, KeyPressed, SelectWizard.
  */
object GameEvent:
  // System events
  case object Initialize extends GameEvent:
    override def priority: Int = 0
  case object Start extends GameEvent:
    override def priority: Int = 0
  case object Stop extends GameEvent:
    override def priority: Int = 0
  case object Pause extends GameEvent:
    override def priority: Int = 1
  case object Resume extends GameEvent:
    override def priority: Int = 1

  // Menu events
  case object ShowMainMenu extends GameEvent:
    override def priority: Int = 2
  case object ShowGameView extends GameEvent:
    override def priority: Int = 2
  case object ShowInfoMenu extends GameEvent:
    override def priority: Int = 2
  case object ExitGame extends GameEvent:
    override def priority: Int = 0

  // Game State events
  case object GameWon extends GameEvent:
    override def priority: Int = 1
  case object GameLost extends GameEvent:
    override def priority: Int = 1
  case object ContinueBattle extends GameEvent:
    override def priority: Int = 2
  case object NewGame extends GameEvent:
    override def priority: Int = 2

  // Input events
  case class GridClicked(logicalPos: LogicalCoords, screenX: Int, screenY: Int) extends GameEvent:
    override def priority: Int = 3
  case class KeyPressed(keyCode: String) extends GameEvent:
    override def priority: Int = 3

  // Wizard selection event
  case class SelectWizard(wizardType: WizardType) extends GameEvent:
    override def priority: Int = 3

/** A queue to manage game events with a maximum size limit.
  *
  * @param queue the underlying queue of game events
  * @param maxQueueSize the maximum size of the queue
  */
case class EventQueue(
    private val queue: Queue[GameEvent] = Queue.empty,
    private val maxQueueSize: Int = 1000
):

  /** Enqueues a new event if the queue is not full.
    *
    * @param event the event to be added to the queue
    * @return a new EventQueue with the event added, or the same queue if full
    */
  def enqueue(event: GameEvent): EventQueue =
    Option.when(queue.size < maxQueueSize)(
      copy(queue = queue.enqueue(event))
    ).getOrElse(this)

  /** Enqueues multiple events.
    *
    * @param events the list of events to be added to the queue
    * @return a new EventQueue with the events added
    */
  def enqueueAll(events: List[GameEvent]): EventQueue =
    events.foldLeft(this)(_.enqueue(_))

  /** Dequeues the next event from the queue.
    *
    * @return a tuple containing the new EventQueue and an Option with the dequeued event
    */
  private def dequeue(): (EventQueue, Option[GameEvent]) =
    queue.dequeueOption match
      case Some((event, newQueue)) => (copy(queue = newQueue), Some(event))
      case None                    => (this, None)

  /** Dequeues all events from the queue.
    *
    * @return a tuple containing an empty EventQueue and a list of all dequeued events
    */
  def dequeueAll(): (EventQueue, List[GameEvent]) =
    (copy(queue = Queue.empty), queue.toList)

  /** Utility methods */
  def isEmpty: Boolean            = queue.isEmpty
  def size: Int                   = queue.size
  def peekNext: Option[GameEvent] = queue.headOption

  /** Filters events in the queue based on a predicate.
    *
    * @param predicate the function to test each event
    * @return a new EventQueue containing only events that satisfy the predicate
    */
  def filter(predicate: GameEvent => Boolean): EventQueue =
    copy(queue = queue.filter(predicate))

  /** Prioritizes events in the queue based on their priority value.
    * Events with lower priority values are moved to the front of the queue.
    *
    * @return a new EventQueue with events sorted by priority
    */
  def prioritize(): EventQueue =
    copy(queue = Queue.from(queue.toList.sortBy(_.priority)))

  // Monadic operations

  /** Applies a function to each event in the queue.
    *
    * @param f the function to apply to each event
    * @return a new EventQueue with the transformed events
    */
  def map(f: GameEvent => GameEvent): EventQueue =
    copy(queue = queue.map(f))

  /** Applies a function that returns a Queue of events to each event in the queue,
    * and flattens the result into a single EventQueue.
    *
    * @param f the function to apply to each event
    * @return a new EventQueue with the flattened events
    */
  def flatMap(f: GameEvent => Queue[GameEvent]): EventQueue =
    copy(queue = queue.flatMap(f))

  /** Folds the events in the queue from the left using a binary operator.
    *
    * @param initial the initial value for the fold
    * @param f the binary operator to apply
    * @tparam B the type of the accumulated value
    * @return the final accumulated value
    */
  def fold[B](initial: B)(f: (B, GameEvent) => B): B =
    queue.foldLeft(initial)(f)

/** Companion object for EventQueue providing utility methods. */
object EventQueue:

  /** Create a new empty EventQueue
    *
    * @return an empty EventQueue
    */
  def empty: EventQueue = EventQueue()

  /** Create an EventQueue from a sequence of events
    *
    * @param events the sequence of events to initialize the queue
    * @return an EventQueue containing the provided events
    */
  def from(events: Seq[GameEvent]): EventQueue =
    EventQueue(Queue.from(events))

  /** Processes the events in the queue as a lazy stream.
    *
    * @param queue the EventQueue to process
    * @return a LazyList of GameEvents
    */
  def processAsStream(queue: EventQueue): LazyList[GameEvent] =
    LazyList.unfold(queue): q =>
      q.dequeue() match
        case (newQueue, Some(event)) => Some((event, newQueue))
        case _                       => None
