package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.controller.GameEvent.{GameLost, GameWon}
import it.unibo.pps.wvt.ecs.components.{PositionComponent, WizardType}
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.systems.{CollisionSystem, CombatSystem, ElixirSystem, HealthSystem, MovementSystem, RenderSystem, SpawnSystem}
import it.unibo.pps.wvt.utilities.GridMapper
import it.unibo.pps.wvt.view.ViewController


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

  def updateAll(world: World): GameSystemsState =
    val updatedElixir = elixir.update(world).asInstanceOf[ElixirSystem]
    val updatedMovement = movement.update(world).asInstanceOf[MovementSystem]
    val updatedCombat = combat.update(world).asInstanceOf[CombatSystem]
    val updatedCollision = collision.update(world).asInstanceOf[CollisionSystem]
    val updatedHealth = health
      .copy(elixirSystem = updatedElixir)
      .update(world)
      .asInstanceOf[HealthSystem]
    val finalElixir = updatedHealth.elixirSystem
    val syncedSpawn = spawn.copy(currentWave = currentWave)
    val updatedSpawn = syncedSpawn.update(world).asInstanceOf[SpawnSystem]
    val updatedRender = render.update(world).asInstanceOf[RenderSystem]
    val newState = copy(
      movement = updatedMovement,
      collision = updatedCollision,
      combat = updatedCombat,
      elixir = finalElixir,
      health = updatedHealth,
      spawn = updatedSpawn,
      render = updatedRender
    )
    checkGameConditions(world)
      .foreach(event => ViewController.getController.foreach(_.postEvent(event)))

    newState

  private def checkGameConditions(world: World): Option[GameEvent] =
    checkWinCondition(world)
      .orElse(checkLoseCondition(world))

  private def checkWinCondition(world: World): Option[GameEvent] =
    Option.when(
      spawn.hasSpawnedAtLeastOnce &&
        !spawn.isActive &&
        world.getEntitiesByType("troll").isEmpty &&
        spawn.getPendingSpawnsCount == 0
    )(GameWon)

  private def checkLoseCondition(world: World): Option[GameEvent] =
    LazyList.from(world.getEntitiesByType("troll"))
      .flatMap(entity => world.getComponent[PositionComponent](entity))
      .find(pos => pos.position.x <= GridMapper.getCellBounds(0,0)._1 + 1e-3)
      .map(_ => GameLost)

  def spendElixir(amount: Int): Option[GameSystemsState] =
    elixir.spendElixir(amount) match
      case (newElixirSystem, true) =>
        Some(copy(
          elixir = newElixirSystem,
          health = health.copy(elixirSystem = newElixirSystem)
        ))
      case _ => None

  def selectWizard(wizardType: WizardType): GameSystemsState =
    copy(selectedWizardType = Some(wizardType))

  def clearWizardSelection: GameSystemsState =
    copy(selectedWizardType = None)

  def handleVictory(): GameSystemsState =
    val nextWave = currentWave + 1
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

  def handleDefeat(): GameSystemsState =
    GameSystemsState.initial()

  def reset(): GameSystemsState =
    GameSystemsState.initial()

  // Accessors
  def getTrollsSpawned: Int = spawn.getTrollsSpawned
  def getMaxTrolls: Int = spawn.getMaxTrolls
  def getCurrentElixir: Int = elixir.getCurrentElixir
  def canAfford(cost: Int): Boolean = elixir.canAfford(cost)
  def getCurrentWave: Int = currentWave

private[controller] object GameSystemsState:
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