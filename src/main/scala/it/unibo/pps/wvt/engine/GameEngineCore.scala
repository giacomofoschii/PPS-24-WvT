package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.model._

// Events that can occur in the game
sealed trait GameEvent

object GameEvent {
  case class SpawnTroll(troll: Troll, pos: Position) extends GameEvent
  case class PlaceWizard(wizard: Wizard, pos: Position) extends GameEvent
  case class FireProjectile(sourceEntity: Entity with Attacker) extends GameEvent
  case class EntityDamaged(entityID: String, damage: Int) extends GameEvent
  case class EntityDestroyed(entityID: String) extends GameEvent
  case class ElixirGenerated(amount: Int) extends GameEvent
  case class WaveCompleted(waveNum: Int) extends GameEvent
  case object GameTick extends GameEvent
  case object PauseGame extends GameEvent
  case object ResumeGame extends GameEvent
  case object StartWave extends GameEvent
  case object EndGame extends GameEvent
}

// Different phases of the game
sealed trait GamePhase

object GamePhase {
  case object Menu extends GamePhase
  case object InGame extends GamePhase
  case object Paused extends GamePhase
  case object WaveCompleted extends GamePhase
  case object GameOver extends GamePhase
}

// Game state representation
case class GameState(
  phase: GamePhase = GamePhase.Menu,
  wizards: List[Wizard] = List.empty,
  trolls: List[Troll] = List.empty,
  projectiles: List[Projectile] = List.empty,
  // Starting elixir amount
  elixir: Int = 200,
  waveNumber: Int = 1,
  trollsToSpawn: List[Troll] = List.empty,
  grid: Grid = Grid(),
  elixirGenerationTimer: Int = 0
  ) {
  def isWaveComplete: Boolean = trolls.isEmpty && trollsToSpawn.isEmpty

  def getEntity(id: String): Option[Entity] =
    (wizards ++ trolls).find(_.id == id)

  def removeEnitity(id: String): GameState =
    copy(
      wizards = wizards.filterNot(_.id == id),
      trolls = trolls.filterNot(_.id == id)
    )

  def updateEntity(entity: Entity): GameState = entity match {
    case wiz: Wizard => copy(wizards = wizards.map(w => if (w.id == wiz.id) wiz else w))
    case tr: Troll => copy(trolls = trolls.map(t => if (t.id == tr.id) tr else t))
  }
}

trait EventHandler {
  def handle(event: GameEvent, state: GameState): (GameState, List[GameEvent])
}

abstract class BaseEventHandler extends EventHandler {

  protected def validateEvent(event: GameEvent, state: GameState): Boolean = true

  protected def processEvent(event: GameEvent, state: GameState): (GameState, List[GameEvent])

  override final def handle(event: GameEvent, state: GameState): (GameState, List[GameEvent]) =
    if(validateEvent(event, state))
      processEvent(event, state)
    else
      (state, List.empty)
}


