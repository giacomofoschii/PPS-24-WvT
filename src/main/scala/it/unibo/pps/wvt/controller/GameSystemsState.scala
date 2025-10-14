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
  
  def updateAll(world: World): (World, GameSystemsState) =
    val (world1, updatedElixir) = elixir.update(world)
    val (world2, updatedMovement) = movement.update(world1)
    val (world3, updatedCombat) = combat.update(world2)
    val (world4, updatedCollision) = collision.update(world3)

    val healthWithElixir = health.copy(elixirSystem = updatedElixir.asInstanceOf[ElixirSystem])
    val (world5, updatedHealth) = healthWithElixir.update(world4)

    val finalElixir = updatedHealth.asInstanceOf[HealthSystem].elixirSystem

    val syncedSpawn = spawn.copy(currentWave = currentWave)
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
      .find(pos => pos.position.x <= GridMapper.getCellBounds(0, 0)._1 + 1e-3)
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