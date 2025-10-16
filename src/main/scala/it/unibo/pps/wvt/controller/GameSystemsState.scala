package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.controller.GameEvent.{GameLost, GameWon}
import it.unibo.pps.wvt.ecs.components.{PositionComponent, WizardType}
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.systems.{
  CollisionSystem,
  CombatSystem,
  ElixirSystem,
  HealthSystem,
  MovementSystem,
  RenderSystem,
  SpawnSystem
}
import it.unibo.pps.wvt.utilities.GridMapper
import it.unibo.pps.wvt.view.ViewController

/** Represents the state of all game systems and provides methods to update and interact with them. */
private[controller] case class GameSystemsState(
    movement: MovementSystem,
    collision: CollisionSystem,
    combat: CombatSystem,
    elixir: ElixirSystem,
    health: HealthSystem,
    spawn: SpawnSystem,
    render: RenderSystem,
    selectedWizardType: Option[WizardType] = None,
    currentWave: Int = 1
):

  /** Updates all game systems in a specific order to ensure correct interactions between them.
    * The order of updates is crucial for maintaining game logic integrity.
    *
    * @param world The current state of the game world.
    * @return A tuple containing the updated world and the new state of all game systems.
    */
  def updateAll(world: World): (World, GameSystemsState) =
    val (world1, updatedElixir)    = elixir.update(world)
    val (world2, updatedMovement)  = movement.update(world1)
    val (world3, updatedCombat)    = combat.update(world2)
    val (world4, updatedCollision) = collision.update(world3)

    val healthWithElixir        = health.copy(elixirSystem = updatedElixir.asInstanceOf[ElixirSystem])
    val (world5, updatedHealth) = healthWithElixir.update(world4)

    val finalElixir = updatedHealth.asInstanceOf[HealthSystem].elixirSystem

    val syncedSpawn            = spawn.copy(currentWave = currentWave)
    val (world6, updatedSpawn) = syncedSpawn.update(world5)

    val (world7, updatedRender) = render.update(world6)

    val newState = copy(
      movement = updatedMovement.asInstanceOf[MovementSystem],
      collision = updatedCollision.asInstanceOf[CollisionSystem],
      combat = updatedCombat.asInstanceOf[CombatSystem],
      elixir = finalElixir,
      health = updatedHealth.asInstanceOf[HealthSystem],
      spawn = updatedSpawn.asInstanceOf[SpawnSystem],
      render = updatedRender.asInstanceOf[RenderSystem]
    )

    checkGameConditions(world7)
      .foreach(event => ViewController.getController.foreach(_.postEvent(event)))

    (world7, newState)

  /** Checks for win or lose conditions in the game.
    *
    * @param world The current state of the game world.
    * @return An optional GameEvent indicating a win or loss, if any condition is met.
    */
  private def checkGameConditions(world: World): Option[GameEvent] =
    checkWinCondition(world)
      .orElse(checkLoseCondition(world))

  /** Checks if the win condition is met.
    * The win condition is satisfied if:
    * - At least one spawn has occurred.
    * - The spawn system is no longer active (no more spawns are pending).
    * - There are no remaining "troll" entities in the world.
    * - There are no pending spawns left in the spawn system.
    *
    * @param world The current state of the game world.
    * @return An optional GameEvent indicating a win if the condition is met.
    */
  private[controller] def checkWinCondition(world: World): Option[GameEvent] =
    Option.when(
      spawn.hasSpawnedAtLeastOnce &&
        !spawn.isActive &&
        world.getEntitiesByType("troll").isEmpty &&
        spawn.getPendingSpawnsCount == 0
    )(GameWon)

  /** Checks if the lose condition is met.
    * The lose condition is satisfied if any "troll" entity has reached the leftmost
    * boundary of the game grid (x <= 0).
    *
    * @param world The current state of the game world.
    * @return An optional GameEvent indicating a loss if the condition is met.
    */
  private[controller] def checkLoseCondition(world: World): Option[GameEvent] =
    LazyList.from(world.getEntitiesByType("troll"))
      .flatMap(entity => world.getComponent[PositionComponent](entity))
      .find(pos => pos.position.x <= GridMapper.getCellBounds(0, 0)._1 + 1e-3)
      .map(_ => GameLost)

  /** Attempts to spend a specified amount of elixir.
    * If the elixir system has enough resources, it deducts the amount and updates
    * the health system accordingly.
    *
    * @param amount The amount of elixir to spend.
    * @return An Option containing the updated GameSystemsState if the spend was successful, or None if not enough elixir.
    */
  def spendElixir(amount: Int): Option[GameSystemsState] =
    elixir.spendElixir(amount) match
      case (newElixirSystem, true) =>
        Some(copy(
          elixir = newElixirSystem,
          health = health.copy(elixirSystem = newElixirSystem)
        ))
      case _ => None

  /** Selects a wizard type for placement in the game.
    *
    * @param wizardType The type of wizard to select.
    * @return A new GameSystemsState with the selected wizard type.
    */
  def selectWizard(wizardType: WizardType): GameSystemsState =
    copy(selectedWizardType = Some(wizardType))

  /** Clears the currently selected wizard type.
    *
    * @return A new GameSystemsState with no wizard type selected.
    */
  def clearWizardSelection: GameSystemsState =
    copy(selectedWizardType = None)

  /** Handles the victory condition by preparing the game state for the next wave.
    * This involves resetting various systems and incrementing the current wave counter.
    *
    * @return A new GameSystemsState ready for the next wave.
    */
  def handleVictory(): GameSystemsState =
    val nextWave    = currentWave + 1
    val freshElixir = ElixirSystem()

    GameSystemsState(
      movement = MovementSystem(),
      collision = CollisionSystem(),
      combat = CombatSystem(),
      elixir = freshElixir,
      health = HealthSystem(freshElixir, Set.empty),
      spawn = SpawnSystem().copy(currentWave = nextWave),
      render = RenderSystem(),
      currentWave = nextWave
    )

  /** Handles the defeat condition by resetting the game state to its initial configuration.
    *
    * @return A new GameSystemsState representing the initial game state.
    */
  def handleDefeat(): GameSystemsState =
    GameSystemsState.initial()

  /** Resets the game systems to their initial state without changing the current wave.
    *
    * @return A new GameSystemsState with all systems reset.
    */
  def reset(): GameSystemsState =
    val freshElixir = ElixirSystem()
    GameSystemsState(
      movement = MovementSystem(),
      collision = CollisionSystem(),
      combat = CombatSystem(),
      elixir = freshElixir,
      health = HealthSystem(freshElixir, Set.empty),
      spawn = SpawnSystem(),
      render = RenderSystem()
    )

  // Accessors
  def getTrollsSpawned: Int         = spawn.getTrollsSpawned
  def getMaxTrolls: Int             = spawn.getMaxTrolls
  def getCurrentElixir: Int         = elixir.getCurrentElixir
  def canAfford(cost: Int): Boolean = elixir.canAfford(cost)
  def getCurrentWave: Int           = currentWave

/** Companion object for GameSystemsState providing an initial state factory method. */
private[controller] object GameSystemsState:

  /** Creates an initial GameSystemsState with all systems set to their default configurations.
    *
    * @param wave The starting wave number, defaulting to 1.
    * @return A new GameSystemsState representing the initial game state.
    */
  def initial(wave: Int = 1): GameSystemsState =
    val elixir = ElixirSystem()
    GameSystemsState(
      movement = MovementSystem(),
      combat = CombatSystem(),
      collision = CollisionSystem(),
      elixir = elixir,
      health = HealthSystem(elixir, Set.empty),
      spawn = SpawnSystem().copy(currentWave = wave),
      render = RenderSystem(),
      currentWave = wave
    )
