package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.engine.GameEvent.ElixirGenerated
import it.unibo.pps.wvt.engine.GamePhase.Paused
import it.unibo.pps.wvt.engine.handlers.*
import it.unibo.pps.wvt.model.*
import it.unibo.pps.wvt.utilities.GameConstants.*

import scala.collection.mutable

trait GameEngine {
  type State = GameState
  type Event = GameEvent

  def currentState: State
  def processEvent(e: Event): Unit
  def update(delta: Long): Unit
  def isRunning: Boolean
}

// Monad for event processing
case class EventResult[A](state: A, events: List[GameEvent]) {
  def map[B](f: A => B): EventResult[B] =
    EventResult(f(state), events)

  def flatMap[B](f: A => EventResult[B]): EventResult[B] =
    val res = f(state)
    EventResult(res.state, events ++ res.events)
}

object EventResult {
  def pure[A](state: A): EventResult[A] = EventResult(state, List.empty)
}

// Main game engine
class GameEngineImpl extends GameEngine {

  private var _currentState: GameState = GameState()
  private val eventQueue: mutable.Queue[GameEvent] = mutable.Queue.empty
  private var _isRunning: Boolean = true
  private var lastElixirGen: Long = System.currentTimeMillis()
  private var lastSpawnTime: Long = System.currentTimeMillis()
  
  private val handlers: Map[Class[_], EventHandler] = Map(
    classOf[GameEvent.SpawnTroll] -> new SpawnHandler(),
    classOf[GameEvent.PlaceWizard] -> new PlacementHandler(),
    classOf[GameEvent.FireProjectile] -> new CombatHandler(),
    classOf[GameEvent.EntityDamaged] -> new CombatHandler(),
    classOf[GameEvent.EntityDestroyed] -> new CombatHandler(),
    classOf[GameEvent.ElixirGenerated] -> new ElixirHandler(),
    classOf[GameEvent.PauseGame.type] -> new PhaseHandler(),
    classOf[GameEvent.ResumeGame.type] -> new PhaseHandler(),
    classOf[GameEvent.StartWave.type] -> new PhaseHandler(),
    classOf[GameEvent.WaveCompleted] -> new PhaseHandler(),
    classOf[GameEvent.EndGame.type] -> new PhaseHandler() 
  )

  override def currentState: GameState = _currentState
  override def isRunning: Boolean = _isRunning

  override def processEvent(e: GameEvent): Unit =
    eventQueue.enqueue(e)

  override def update(delta: Long): Unit =
    processEventQueue()
    if (_currentState.phase == GamePhase.InGame)
      processAutomaticEvents(delta)
      updateGameLogic(delta)

  private def processAutomaticEvents(delta: Long): Unit =
    val currentTime = System.currentTimeMillis()

    if(currentTime - lastElixirGen >= ELIXIR_GENERATION_INTERVAL)
      processEvent(ElixirGenerated(PERIODIC_ELIXIR))
      _currentState.wizards.collect {
        case gen: GeneratorWizard => GameEvent.ElixirGenerated(gen.generateElixir)
      }.foreach(processEvent)
      lastElixirGen = currentTime

    if(currentTime - lastSpawnTime >= INITIAL_SPAWN_INTERVAL && _currentState.trollsToSpawn.nonEmpty)
      _currentState.trollsToSpawn.headOption.foreach { troll =>
        processEvent(GameEvent.SpawnTroll(troll))
        _currentState = _currentState.copy(trollsToSpawn = _currentState.trollsToSpawn.tail)
      }
      lastSpawnTime = currentTime

    processAttacks()

  private def processAttacks(): Unit =
    _currentState.wizards.collect {
      case wiz: Wizard with Attacker => wiz }.foreach { wizard =>
        findNearestEnemy(wizard, _currentState.trolls).foreach { _ =>
          processEvent(GameEvent.FireProjectile(wizard))
        }
      }

    _currentState.trolls.foreach { troll =>
      findNearestEnemy(troll, _currentState.wizards).foreach { _ =>
        processEvent(GameEvent.FireProjectile(troll))
      }
    }

  private def findNearestEnemy[T <: Entity](attacker: Entity, targets: List[T]): Option[T] =
    targets
      .filter(_.isAlive)
      .filter(t => isInRange(attacker, t))
      .sortBy(t => distance(attacker.position, t.position))
      .headOption

  private def isInRange(attacker: Entity, target: Entity): Boolean =
    val range = attacker match
      case w: Wizard => w.range
      case _: Troll => 1
      case _ => 0

    distance(attacker.position, target.position) <= range

  private def distance(p1: Position, p2: Position): Double =
    math.sqrt(math.pow(p1.row - p2.row, 2) + math.pow(p1.col - p2.col, 2))

  private def processEventQueue(): Unit =
    while(eventQueue.nonEmpty)
      val event = eventQueue.dequeue()
      val handler = handlers.get(event.getClass)
        .orElse(handlers.find(_._1.isAssignableFrom(event.getClass)).map(_._2))
      
      handler.foreach { h =>
        val (newState, newEvents) = h.handle(event, _currentState)
        _currentState = newState
        newEvents.foreach(eventQueue.enqueue)
      }
      
  private def updateGameLogic(delta: Long): Unit =
    // Update projectiles
    updateProjectiles(delta)
    
    // Update trolls
    updateTrolls(delta)
    
    // Check for game over conditions
    checkGameOver()
    
  private def updateProjectiles(delta: Long): Unit =
    val updatedProjectiles = _currentState.projectiles.flatMap { projectile =>
      val targets = projectile.projectileType match
        case ProjectileType.TrollAttack => _currentState.wizards
        case _ => _currentState.trolls

      targets.find(t => distance(Position(0, 0), t.position) < 1.0) match
        case Some(target) =>
          processEvent(GameEvent.EntityDamaged(target.id, projectile.damage))
          None // Projectile hits the target and is removed
        case None =>
          Some(projectile)
    }
    _currentState = _currentState.copy(projectiles = updatedProjectiles)

  private def updateTrolls(delta: Long): Unit =
    val updatedTrolls = _currentState.trolls.map { troll =>
      if(troll.position.col > 0)
        val newPos = Position(troll.position.row, troll.position.col - troll.speed)
        if(_currentState.grid.isEmpty(newPos))
          val oldGrid = _currentState.grid.set(troll.position, CellType.Empty)
          val newGrid = oldGrid.set(newPos, CellType.Troll)
          _currentState = _currentState.copy(grid = newGrid)
          troll.updatePosition(newPos).asInstanceOf[Troll]
        else 
          troll
      else
        processEvent(GameEvent.EndGame)
        troll
    }
    _currentState = _currentState.copy(trolls = updatedTrolls)

  private def checkGameOver(): Unit =
    if(_currentState.trolls.exists(_.position.col == 0))
      processEvent(GameEvent.EndGame)
      _isRunning = false
}

object GameEngine {
  def create(): GameEngine = new GameEngineImpl()
  
  extension (engine: GameEngine) {
    private def withEvent(e: GameEvent): GameEngine =
      engine.processEvent(e)
      engine
      
    def startGame(): GameEngine =
      engine.withEvent(GameEvent.StartWave)
      
    def pauseGame(): GameEngine =
      engine.withEvent(GameEvent.PauseGame)
      
    def resumeGame(): GameEngine =
      engine.withEvent(GameEvent.ResumeGame)
  }
}

