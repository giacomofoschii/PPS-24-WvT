package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.utilities.Position
import scala.collection.immutable.Queue

enum GameEvent:
  // System events
  case Initialize
  case Start
  case Stop
  case Pause
  case Resume

  // Menu events
  case ShowMainMenu
  case ShowGameView
  case ShowInfoMenu
  case ExitGame

  case GameWon
  case GameLost
  case ContinueBattle
  case NewGame

  // Input events
  case GridClicked(pos: Position, screenX: Int, screenY: Int)
  case KeyPressed(keyCode: String)

  def timestamp: Long = System.currentTimeMillis()

  // Pattern matching for event priority
  def priority: Int = this match
    case Stop | ExitGame => 0
    case Pause | Resume => 1
    case ShowMainMenu | 
         ShowGameView | 
         ShowInfoMenu | 
         ContinueBattle | 
         NewGame => 2
    case _ => 3

class EventQueue(private val maxQueueSize: Int = 1000):
  private var queue: Queue[GameEvent] = Queue.empty

  def enqueue(event: GameEvent): Boolean =
    if queue.size < maxQueueSize then
      queue = queue.enqueue(event)
      true
    else
      println(s"Event queue full, dropping event: $event")
      false

  def dequeue(): Option[GameEvent] =
    queue.dequeueOption.map: (event, newQueue) =>
      queue = newQueue
      event

  def dequeueAll(): List[GameEvent] =
    val events = queue.toList
    queue = Queue.empty
    events

  def isEmpty: Boolean = queue.isEmpty

  def size: Int = queue.size

  def clear(): Unit =
    queue = Queue.empty

  def peekNext: Option[GameEvent] = queue.headOption

  def filterQueue(predicate: GameEvent => Boolean): Unit =
    queue = queue.filter(predicate)

  def prioritize(): Unit =
    queue = Queue.from(queue.toList.sortBy(_.priority))