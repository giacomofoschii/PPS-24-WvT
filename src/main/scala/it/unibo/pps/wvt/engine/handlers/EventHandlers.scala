package it.unibo.pps.wvt.engine.handlers

import it.unibo.pps.wvt.engine._
import it.unibo.pps.wvt.model._
import it.unibo.pps.wvt.utilities.GamePlayConstants._
import it.unibo.pps.wvt.utilities.ViewConstants._

trait EntityManagement {
  self: BaseEventHandler =>

  protected def spawnTroll(troll: Troll, state: GameState): GameState =
    val updatedGrid = state.grid.set(troll.position, Cell(troll.position, CellType.Troll))
    state.copy(
      trolls = troll :: state.trolls,
      grid = updatedGrid
    )

  protected def placeWizard(wizard: Wizard, state: GameState): Option[GameState] =
    if (state.elixir >= wizard.cost 
      && state.grid.isCellEmpty(wizard.position) 
      && state.grid.isValidPosition(wizard.position))
      val updatedGrid = state.grid.set(wizard.position, Cell(wizard.position, CellType.Wizard))
      Some(
        state.copy(
          wizards = wizard :: state.wizards,
          elixir = state.elixir - wizard.cost,
          grid = updatedGrid
        )
      )
    else None
}

trait CombatMechanics {
  self: BaseEventHandler =>

  protected def handleProjectile(sourceEntity: Entity with Attacker, state: GameState): List[Projectile] =
    sourceEntity.attack().map(_ :: state.projectiles).getOrElse(state.projectiles)

  protected def applyDamage(entityID: String, damage: Int, state: GameState): (GameState, Option[GameEvent.EntityDestroyed]) =
    state.getEntity(entityID) match
      case Some(entity) =>
        val damaged = entity.takeDamage(damage)
        if (damaged.isAlive)
          (state.updateEntity(damaged), None)
        else
          val updatedGrid = state.grid.emptyCell(entity.position)
          val updatedState = state.removeEntity(entityID).copy(grid = updatedGrid)

          val finalState = entity match
            case _: BaseTroll => updatedState.copy(elixir = updatedState.elixir + BASE_TROLL_REWARD)
            case _: WarriorTroll => updatedState.copy(elixir = updatedState.elixir + WARRIOR_TROLL_REWARD)
            case _: AssassinTroll => updatedState.copy(elixir = updatedState.elixir + ASSASSIN_TROLL_REWARD)
            case _: ThrowerTroll => updatedState.copy(elixir = updatedState.elixir + THROWER_TROLL_REWARD)
            case _ => updatedState

          (finalState, Some(GameEvent.EntityDestroyed(entityID)))

      case None => (state, None)
}

// Troll Spawn Handler
class SpawnHandler extends BaseEventHandler with EntityManagement {
  override protected def validateEvent(event: GameEvent, state: GameState): Boolean = 
    event match
      case GameEvent.SpawnTroll(_) =>
        state.phase == GamePhase.InGame
      
      case _ => false

  override protected def processEvent(event: GameEvent, state: GameState): (GameState, List[GameEvent]) =
    event match
      case GameEvent.SpawnTroll(troll) =>
        (spawnTroll(troll, state), List.empty)
      
      case _ => (state, List.empty)
}

// Wizard Placement Handler
class PlacementHandler extends BaseEventHandler with EntityManagement {

  override protected def validateEvent(event: GameEvent, state: GameState): Boolean =
    event match
      case GameEvent.PlaceWizard(_, _) =>
        state.phase == GamePhase.InGame
      
      case _ => false

  override protected def processEvent(event: GameEvent, state: GameState): (GameState, List[GameEvent]) =
    event match
      case GameEvent.PlaceWizard(wizard, pos) =>
        val wizardWithPos = wizard match
          case gen: GeneratorWizard => gen.copy(position = pos)
          case wind: WindWizard => wind.copy(position = pos)
          case barr: BarrierWizard => barr.copy(position = pos)
          case fire: FireWizard => fire.copy(position = pos)
          case ice: IceWizard => ice.copy(position = pos)

        placeWizard(wizardWithPos, state) match
          case Some(newState) => (newState, List.empty)
          case None => (state, List.empty) // Invalid placement
      
      case _ => (state, List.empty)
}

// Combat Handler
class CombatHandler extends BaseEventHandler with CombatMechanics {
  override protected def processEvent(event: GameEvent, state: GameState): (GameState, List[GameEvent]) =
    event match
      case GameEvent.FireProjectile(sourceEntity) =>
        val updatedProjectiles = handleProjectile(sourceEntity, state)
        (state.copy(projectiles = updatedProjectiles), List.empty)

      case GameEvent.EntityDamaged(entityID, damage) =>
        val (newState, maybeDestroyed) = applyDamage(entityID, damage, state)
        val events = maybeDestroyed.toList
        if (newState.isWaveCompleted && events.nonEmpty)
          (newState, events :+ GameEvent.WaveCompleted(newState.waveNumber))
        else
          (newState, events)

      case GameEvent.EntityDestroyed(entityID) =>
        if (state.isWaveCompleted)
          (state, List(GameEvent.WaveCompleted(state.waveNumber)))
        else
          (state, List.empty)

      case _ => (state, List.empty)
}

// Elixir Generation Handler
class ElixirHandler extends BaseEventHandler {
  override protected def processEvent(event: GameEvent, state: GameState): (GameState, List[GameEvent]) =
    event match
      case GameEvent.ElixirGenerated(amount) =>
        (state.copy(elixir = state.elixir + amount), List.empty)
     
      case _ => (state, List.empty)
}

//Phase Handler
class PhaseHandler extends BaseEventHandler {
  override protected def processEvent(event: GameEvent, state: GameState): (GameState, List[GameEvent]) =
    event match
      case GameEvent.PauseGame if state.phase == GamePhase.InGame =>
        (state.copy(phase = GamePhase.Paused), List.empty)

      case GameEvent.ResumeGame if state.phase == GamePhase.Paused =>
        (state.copy(phase = GamePhase.InGame), List.empty)

      case GameEvent.StartWave =>
        state.phase match
          case GamePhase.Menu =>
            (state.copy(
              phase = GamePhase.InGame,
              trollsToSpawn = generateWaveTrolls(state.waveNumber)
            ), List.empty)

          case GamePhase.WaveComplete =>
            val nextWave = state.waveNumber + 1
            (state.copy(
              phase = GamePhase.InGame,
              waveNumber = nextWave,
              trollsToSpawn = generateWaveTrolls(nextWave)
            ), List.empty)

          case _ => (state, List.empty)

      case GameEvent.WaveCompleted(_) =>
        (state.copy(phase = GamePhase.WaveComplete), List.empty)

      case GameEvent.EndGame =>
        (state.copy(phase = GamePhase.GameOver), List.empty)

      case _ => (state, List.empty)

  private def generateWaveTrolls(waveNum: Int): List[Troll] =
    val baseCount = 3 + waveNum
    (1 to baseCount).map { i =>
      BaseTroll(
        id = s"troll-w$waveNum-$i",
        position = Position(i % GRID_ROWS, GRID_COLS - 1),
        health = BASE_TROLL_HEALTH
      )
    }.toList
}

