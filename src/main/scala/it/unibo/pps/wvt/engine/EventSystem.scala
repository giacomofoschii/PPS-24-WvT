package it.unibo.pps.wvt.engine

import scala.collection.mutable


sealed trait GameEvent {
  def timestamp: Long = System.currentTimeMillis()
}

object GameEvent {
  // System events
  case object Initialize extends GameEvent
  case object Start extends GameEvent
  case object Stop extends GameEvent
  case object Pause extends GameEvent
  case object Resume extends GameEvent

  // Menu events
  case object ShowMainMenu extends GameEvent
  case object ShowGameView extends GameEvent
  case object ShowInfoMenu extends GameEvent
  case object ExitGame extends GameEvent

  // Update event
  case class Update(deltaTime: Long) extends GameEvent

  // Render event
  case object Render extends GameEvent
}

class EventQueue {
  private val queue: mutable.Queue[GameEvent] = mutable.Queue.empty
  private val maxQueueSize: Int = 1000

  def enqueue(event: GameEvent): Boolean =
    if (queue.size < maxQueueSize)
      queue.enqueue(event)
      true
    else
      println(s"Event queue full, dropping event: $event")
      false

  def dequeue(): Option[GameEvent] =
    if (queue.nonEmpty) Some(queue.dequeue())
    else None

  def isEmpty: Boolean = queue.isEmpty

  def size: Int = queue.size

  def clear(): Unit = queue.clear()
}

class EventProcessor {
  private val eventQueue: EventQueue = new EventQueue()
  private val eventHandlers: mutable.Map[Class[_], GameEvent => Unit] = mutable.Map.empty
  
  def registerHandler[T <: GameEvent](eventClass: Class[T], handler: T => Unit): Unit =
    eventHandlers(eventClass) = handler.asInstanceOf[GameEvent => Unit]
  
  def postEvent(event: GameEvent): Unit = 
    eventQueue.enqueue(event)
  
  def processEvents(): Unit =
    while (!eventQueue.isEmpty) 
      eventQueue.dequeue().foreach { event =>
        processEvent(event)
      }
  
  private def processEvent(event: GameEvent): Unit = 
    eventHandlers.get(event.getClass).foreach { handler =>
      try 
        handler(event)
      catch
        case e: Exception =>
          println(s"Error processing event $event: ${e.getMessage}")
          e.printStackTrace()
    }
  
  def clearQueue(): Unit = eventQueue.clear()
}