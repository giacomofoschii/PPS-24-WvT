package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.controller.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class EventQueueTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  var queue: EventQueue = _

  override def beforeEach(): Unit =
    queue = EventQueue()

  "EventQueue" should "start empty" in:
    queue.isEmpty shouldBe true
    queue.size shouldBe 0

  it should "enqueue events correctly" in:
    val event = GameEvent.ShowMainMenu

    queue.enqueue(event) shouldBe true
    queue.isEmpty shouldBe false
    queue.size shouldBe 1

  it should "dequeue events in FIFO order" in:
    val event1 = GameEvent.ShowMainMenu
    val event2 = GameEvent.ShowGameView
    val event3 = GameEvent.Pause

    queue.enqueue(event1)
    queue.enqueue(event2)
    queue.enqueue(event3)

    queue.dequeue() shouldBe Some(event1)
    queue.dequeue() shouldBe Some(event2)
    queue.dequeue() shouldBe Some(event3)
    queue.dequeue() shouldBe None

  it should "return None when dequeuing empty queue" in:
    queue.dequeue() shouldBe None

  it should "peek next event without removing it" in:
    val event = GameEvent.Resume

    queue.enqueue(event)
    queue.peekNext shouldBe Some(event)
    queue.size shouldBe 1               // Still in queue
    queue.peekNext shouldBe Some(event) // Still there

  it should "dequeue all events at once" in:
    val events = List(
      GameEvent.ShowMainMenu,
      GameEvent.ShowGameView,
      GameEvent.Pause
    )

    events.foreach(queue.enqueue)

    val dequeued = queue.dequeueAll()
    dequeued shouldBe events
    queue.isEmpty shouldBe true

  it should "clear all events" in:
    queue.enqueue(GameEvent.ShowMainMenu)
    queue.enqueue(GameEvent.ShowGameView)
    queue.enqueue(GameEvent.Pause)

    queue.size shouldBe 3

    queue.clear()
    queue.isEmpty shouldBe true
    queue.size shouldBe 0

  it should "reject events when queue is full" in:
    val smallQueue = EventQueue(maxQueueSize = 3)

    smallQueue.enqueue(GameEvent.ShowMainMenu) shouldBe true
    smallQueue.enqueue(GameEvent.ShowGameView) shouldBe true
    smallQueue.enqueue(GameEvent.Pause) shouldBe true
    smallQueue.enqueue(GameEvent.Resume) shouldBe false // Queue full

    smallQueue.size shouldBe 3

  it should "filter queue by predicate" in:
    queue.enqueue(GameEvent.ShowMainMenu)
    queue.enqueue(GameEvent.Pause)
    queue.enqueue(GameEvent.ShowGameView)
    queue.enqueue(GameEvent.Resume)

    // Keep only menu events
    queue.filterQueue:
      case GameEvent.ShowMainMenu | GameEvent.ShowGameView | GameEvent.ShowInfoMenu => true
      case _                                                                        => false

    queue.size shouldBe 2
    val remaining = queue.dequeueAll()
    remaining should contain(GameEvent.ShowMainMenu)
    remaining should contain(GameEvent.ShowGameView)

  it should "prioritize events correctly" in:
    queue.enqueue(GameEvent.Start)        // priority 3
    queue.enqueue(GameEvent.Stop)         // priority 0
    queue.enqueue(GameEvent.ShowMainMenu) // priority 2
    queue.enqueue(GameEvent.Pause)        // priority 1

    queue.prioritize()

    val events = queue.dequeueAll()
    events.head shouldBe GameEvent.Stop       // priority 0 first
    events(1) shouldBe GameEvent.Pause        // priority 1
    events(2) shouldBe GameEvent.ShowMainMenu // priority 2
    events.last shouldBe GameEvent.Start      // priority 3 last

  it should "handle GridClicked events" in:
    val gridEvent = GameEvent.GridClicked((2, 3), 100, 200)

    queue.enqueue(gridEvent) shouldBe true
    queue.dequeue() shouldBe Some(gridEvent)

  it should "handle KeyPressed events" in:
    val keyEvent = GameEvent.KeyPressed("SPACE")

    queue.enqueue(keyEvent) shouldBe true
    queue.dequeue() shouldBe Some(keyEvent)

  "GameEvent" should "have correct timestamps" in:
    val event     = GameEvent.ShowMainMenu
    val timestamp = event.timestamp

    timestamp should be > 0L
    timestamp should be <= System.currentTimeMillis()

  it should "assign correct priorities" in:
    GameEvent.Stop.priority shouldBe 0
    GameEvent.ExitGame.priority shouldBe 0
    GameEvent.Pause.priority shouldBe 1
    GameEvent.Resume.priority shouldBe 1
    GameEvent.ShowMainMenu.priority shouldBe 2
    GameEvent.ShowGameView.priority shouldBe 2
    GameEvent.ShowInfoMenu.priority shouldBe 2
    GameEvent.Start.priority shouldBe 3
    GameEvent.Initialize.priority shouldBe 3

  it should "handle multiple enqueue/dequeue cycles" in:
    for i <- 1 to 100 do
      queue.enqueue(GameEvent.Start) shouldBe true
      queue.dequeue() shouldBe Some(GameEvent.Start)
      queue.isEmpty shouldBe true
