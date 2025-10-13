package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.utilities.GridMapper.LogicalCoords

import scala.collection.immutable.Queue

trait GameEvent:
  def timestamp: Long = System.currentTimeMillis()
  def priority: Int

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

case class EventQueue(
                       private val queue: Queue[GameEvent] = Queue.empty,
                       private val maxQueueSize: Int = 1000
                     ):
  def enqueue(event: GameEvent): EventQueue =
    Option.when(queue.size < maxQueueSize)(
      copy(queue = queue.enqueue(event))
    ).getOrElse(this)

  def enqueueAll(events: List[GameEvent]): EventQueue =
    events.foldLeft(this)(_.enqueue(_))

  def dequeue(): (EventQueue, Option[GameEvent]) =
    queue.dequeueOption match
      case Some((event, newQueue)) => (copy(queue = newQueue), Some(event))
      case None => (this, None)

  def dequeueAll(): (EventQueue, List[GameEvent]) =
    (copy(queue = Queue.empty), queue.toList)

  def isEmpty: Boolean = queue.isEmpty
  def size: Int = queue.size
  def peekNext: Option[GameEvent] = queue.headOption

  def filter(predicate: GameEvent => Boolean): EventQueue =
    copy(queue = queue.filter(predicate))

  def prioritize(): EventQueue =
    copy(queue = Queue.from(queue.toList.sortBy(_.priority)))

  // Monadic operations
  def map(f: GameEvent => GameEvent): EventQueue =
    copy(queue = queue.map(f))

  def flatMap(f: GameEvent => Queue[GameEvent]): EventQueue =
    copy(queue = queue.flatMap(f))

  def fold[B](initial: B)(f: (B, GameEvent) => B): B =
    queue.foldLeft(initial)(f)

object EventQueue:
  def empty: EventQueue = EventQueue()

  def from(events: Seq[GameEvent]): EventQueue =
    EventQueue(Queue.from(events))

  def processAsStream(queue: EventQueue): LazyList[GameEvent] =
    LazyList.unfold(queue): q =>
      q.dequeue() match
        case (newQueue, Some(event)) => Some((event, newQueue))
        case _ => None