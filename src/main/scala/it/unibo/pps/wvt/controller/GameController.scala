package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.controller.GameEvent.{GameLost, GameWon}
import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.view.{ShopPanel, ViewController, WavePanel}
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.WizardType.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.systems.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import scalafx.application.Platform

import java.util.concurrent.ConcurrentLinkedQueue
import scala.annotation.tailrec

class GameController(world: World):

  case class GameSystemsState(
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

    private def checkGameConditions(world: World): Option[GameEvent] =
      checkWinCondition(world)
        .orElse(checkLoseCondition(world))

    def getCurrentWave: Int = currentWave

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

      val updatedState = copy(
        movement = updatedMovement,
        combat = updatedCombat,
        elixir = finalElixir,
        health = updatedHealth,
        spawn = updatedSpawn,
        render = updatedRender
      )

      updatedState.checkGameConditions(world).foreach: event =>
        ViewController.getController.foreach(_.postEvent(event))

      updatedState

    def spendElixir(amount: Int): Option[GameSystemsState] =
      val (newElixirSystem, success) = elixir.spendElixir(amount)
      Option.when(success):
        copy(
          elixir = newElixirSystem,
          health = health.copy(elixirSystem = newElixirSystem)
        )

    def selectWizard(wizardType: WizardType): GameSystemsState =
      copy(selectedWizardType = Some(wizardType))

    def clearWizardSelection: GameSystemsState =
      copy(selectedWizardType = None)

    def getTrollsSpawned: Int = spawn.getTrollsSpawned
    def getMaxTrolls: Int = spawn.getMaxTrolls
    def getCurrentElixir: Int = elixir.getCurrentElixir
    def canAfford(cost: Int): Boolean = elixir.canAfford(cost)

    def handleVictory(): GameSystemsState =
      val nextWave = currentWave + 1
      val freshElixir = ElixirSystem()
      println("Starting wave " + nextWave)
      copy(
        currentWave = nextWave,
        spawn = SpawnSystem().copy(currentWave = nextWave),
        elixir = freshElixir,
        health = HealthSystem(freshElixir, Set.empty),
        movement = MovementSystem(),
        combat = CombatSystem(),
        collision = CollisionSystem(),
        render = RenderSystem(),
        selectedWizardType = None
      )

    def handleDefeat(): GameSystemsState = GameSystemsState.initial()

    def reset(): GameSystemsState = GameSystemsState.initial()

    private def checkLoseCondition(world: World): Option[GameEvent] =
      @tailrec
      def hasTrollReachedEnd(entities: List[EntityId]): Boolean =
        entities match
          case Nil => false
          case head :: tail =>
            world.getComponent[PositionComponent](head) match
              case Some(pos) =>
                val isAtEnd = pos.position.x <= GridMapper.getCellBounds(0, 0)._1 + 1e-3
                if isAtEnd then true else hasTrollReachedEnd(tail)
              case _ => hasTrollReachedEnd(tail)

      val trolls = world.getEntitiesByType("troll").toList
      Option.when(hasTrollReachedEnd(trolls))(GameLost)

    private def checkWinCondition(world: World): Option[GameEvent] =
      val waveHasStarted = spawn.hasSpawnedAtLeastOnce
      val allTrollsSpawned = !spawn.isActive
      val noActiveTrolls = world.getEntitiesByType("troll").isEmpty
      val noPendingSpawns = spawn.getPendingSpawnsCount == 0

      Option.when(waveHasStarted && allTrollsSpawned && noActiveTrolls && noPendingSpawns)(GameWon)

  private object GameSystemsState:
    def initial(): GameSystemsState =
      val elixir = ElixirSystem()

      GameSystemsState(
        movement = MovementSystem(),
        combat = CombatSystem(),
        collision = CollisionSystem(),
        elixir = elixir,
        health = HealthSystem(elixir, Set.empty),
        spawn = SpawnSystem(),
        render = RenderSystem()
      )

  private type StateTransformation = GameSystemsState => GameSystemsState

  private val gameEngine: GameEngine = new GameEngineImpl()
  private val eventHandler: EventHandler = EventHandler.create(gameEngine)
  private val inputSystem: InputSystem = InputSystem()

  private var state: GameSystemsState = GameSystemsState.initial()
  private var isInitialized: Boolean = false

  private val pendingActions: ConcurrentLinkedQueue[StateTransformation] = new ConcurrentLinkedQueue()

  def initialize(): Unit =
    world.clear()
    state = GameSystemsState.initial()
    pendingActions.clear()

    if !isInitialized then
      gameEngine.initialize(this)
      setupEventHandlers()
      isInitialized = true
    else
      eventHandler.clearQueue()

  @tailrec
  private def processPendingActions(): Unit =
    if !pendingActions.isEmpty then
      val action = pendingActions.poll()
      if action != null then
        state = action(state)
      processPendingActions()

  def update(): Unit =
    if eventHandler.getCurrentPhase == GamePhase.Playing && !gameEngine.isPaused then
      processPendingActions()

      val oldWave = state.getCurrentWave
      state = state.updateAll(world)
      val newWave = state.getCurrentWave

      if oldWave != newWave then
        notifyWaveChange(newWave)

      ViewController.render()

  def getCurrentElixir: Int = state.getCurrentElixir
  def getRenderSystem: RenderSystem = state.render

  def getCurrentWaveInfo: (Int, Int, Int) =
    val wave = state.getCurrentWave
    val spawned = state.getTrollsSpawned
    val max = state.getMaxTrolls
    (wave, spawned, max)

  def postEvent(event: GameEvent): Unit =
    eventHandler.postEvent(event)
    event match
      case GameEvent.Pause | GameEvent.Resume | GameEvent.ShowMainMenu |
           GameEvent.ShowGameView | GameEvent.ShowInfoMenu | GameEvent.ExitGame =>
        eventHandler.processEvents()
      case _ =>
        if isMenuPhase(eventHandler.getCurrentPhase) then
          eventHandler.processEvents()

  def start(): Unit = gameEngine.start()
  def stop(): Unit = gameEngine.stop()
  def pause(): Unit = gameEngine.pause()
  def resume(): Unit = gameEngine.resume()

  def getEngine: GameEngine = gameEngine
  def getInputSystem: InputSystem = inputSystem
  def getEventHandler: EventHandler = eventHandler
  def getWorld: World = world

  def selectWizard(wizardType: WizardType): Unit =
    pendingActions.add: currentState =>
      if currentState.selectedWizardType.contains(wizardType) then
        currentState
      else
        currentState.selectWizard(wizardType)

    val occupiedCells = calculateOccupiedCells()
    val freeCells = GridMapper.allCells.diff(occupiedCells)

    ViewController.drawPlacementGrid(freeCells, occupiedCells)

  private def calculateOccupiedCells(): Seq[Position] =
    val wizards = getWorld.getEntitiesByType("wizard").toList

    @tailrec
    def collectOccupiedPositions(entities: List[EntityId], acc: List[Position]): List[Position] =
      entities match
        case Nil => acc
        case head :: tail =>
          val maybePos = for
            posComp <- getWorld.getComponent[PositionComponent](head)
            logicalCoords <- GridMapper.physicalToLogical(posComp.position)
          yield Position(
            GRID_OFFSET_X + logicalCoords._2 * CELL_WIDTH,
            GRID_OFFSET_Y + logicalCoords._1 * CELL_HEIGHT
          )

          collectOccupiedPositions(tail, maybePos.fold(acc)(acc :+ _))

    collectOccupiedPositions(wizards, List.empty)

  def handleMouseClick(x: Double, y: Double): Unit =
    val clickResult = inputSystem.handleMouseClick(x, y)
    if clickResult.isValid then
      GridMapper.physicalToLogical(clickResult.pos)
        .flatMap(GridMapper.logicalToPhysical)
        .foreach: centeredPos =>
          handleGridClick(centeredPos)
          ViewController.render()

  def handleContinueBattle(): Unit =
    world.clear()
    state = state.handleVictory()
    notifyWaveChange(state.getCurrentWave)
    ViewController.render()

  def handleNewGame(): Unit =
    world.clear()
    state = state.reset()
    notifyWaveChange(state.getCurrentWave)
    ViewController.render()

  private def notifyWaveChange(newWave: Int): Unit =
    Platform.runLater:
      WavePanel.updateWaveNumber(newWave)

  def placeWizard(wizardType: WizardType, position: Position): Unit =
    val cost = ShopPanel.getWizardCost(wizardType)
    val hasWizardCell = world.hasWizardAt(position)
    val canPlace = !hasWizardCell && state.canAfford(cost)

    if !canPlace then
      val errorMessage = if world.getEntityAt(position).isDefined then
        s"Impossible to place ${wizardType.toString}: cell occupied"
      else
        s"Impossible to place ${wizardType.toString}: not enough elixir"

      ViewController.showError(errorMessage)
      ViewController.hidePlacementGrid()
      return

    val entity = createWizardEntity(wizardType, position)
    val isFirstWizard = !state.elixir.firstWizardPlaced

    val transformation: StateTransformation = currentState =>
      currentState.spendElixir(cost) match
        case Some(stateAfterSpending) =>
          if isFirstWizard then
            val activatedElixir = stateAfterSpending.elixir.activateGeneration()
            stateAfterSpending.copy(
              elixir = activatedElixir,
              health = stateAfterSpending.health.copy(elixirSystem = activatedElixir)
            ).clearWizardSelection
          else
            stateAfterSpending.clearWizardSelection
        case None =>
          currentState.clearWizardSelection

    pendingActions.add(transformation)
    ViewController.hidePlacementGrid()

  def placeTroll(trollType: TrollType, position: Position): Unit =
    if world.getEntityAt(position).isEmpty then
      val entity = trollType match
        case TrollType.Base => EntityFactory.createBaseTroll(world, position)
        case TrollType.Warrior => EntityFactory.createWarriorTroll(world, position)
        case TrollType.Assassin => EntityFactory.createAssassinTroll(world, position)
        case TrollType.Thrower => EntityFactory.createThrowerTroll(world, position)
      ViewController.render()

  private def setupEventHandlers(): Unit =
    eventHandler.registerHandler(classOf[GameEvent.GridClicked]): (event: GameEvent.GridClicked) =>
      handleGridClick(GridMapper.logicalToPhysical(event.logicalPos).get)

  private def handleGridClick(position: Position): Unit =
    state.selectedWizardType match
      case Some(wizardType) =>
        placeWizard(wizardType, position)
      case None =>
        ViewController.showError("No wizard selected for placement, please select one from the shop first.")
        ViewController.hidePlacementGrid()

  private def createWizardEntity(wizardType: WizardType, position: Position): EntityId =
    wizardType match
      case Generator => EntityFactory.createGeneratorWizard(world, position)
      case Barrier => EntityFactory.createBarrierWizard(world, position)
      case Wind => EntityFactory.createWindWizard(world, position)
      case Fire => EntityFactory.createFireWizard(world, position)
      case Ice => EntityFactory.createIceWizard(world, position)

  private def isMenuPhase(phase: GamePhase): Boolean =
    phase.isMenu || phase == GamePhase.Paused