package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.view.{ShopPanel, ViewController}
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.WizardType.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.systems.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}

class GameController(world: World):

  case class GameSystemsState(
                               movement: MovementSystem,
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
      val updatedHealth = health
        .copy(elixirSystem = updatedElixir)
        .update(world)
        .asInstanceOf[HealthSystem]
      val updatedSpawn = spawn.update(world).asInstanceOf[SpawnSystem]
      val updatedRender = render.update(world).asInstanceOf[RenderSystem]

      copy(
        movement = updatedMovement,
        combat = updatedCombat,
        elixir = updatedElixir,
        health = updatedHealth,
        spawn = updatedSpawn,
        render = updatedRender
      )

    def spendElixir(amount: Int): Option[GameSystemsState] =
      val (newElixirSystem, success) = elixir.spendElixir(amount)
      if success then
        Some(copy(
          elixir = newElixirSystem,
          health = health.copy(elixirSystem = newElixirSystem)
        ))
      else
        None

    def selectWizard(wizardType: WizardType): GameSystemsState =
      copy(selectedWizardType = Some(wizardType))

    def clearWizardSelection: GameSystemsState =
      copy(selectedWizardType = None)

    def getCurrentWave: Int = currentWave

    def incrementWave(): GameSystemsState = copy(currentWave = currentWave + 1)

    def getCurrentElixir: Int = elixir.getCurrentElixir

    def canAfford(cost: Int): Boolean = elixir.canAfford(cost)

    def reset(): GameSystemsState = GameSystemsState.initial()

  object GameSystemsState:

    def getSpawnIntervalForWave: Long =
      2000L // TODO SPRINT 3, rendere dinamico questo intervallo per le wave a difficoltà crescenti

    def initial(): GameSystemsState =
      val elixir = ElixirSystem()
      GameSystemsState(
        movement = MovementSystem(),
        combat = CombatSystem(),
        elixir = elixir,
        health = HealthSystem(elixir, Set.empty),
        spawn = SpawnSystem(getSpawnIntervalForWave),
        render = RenderSystem()
      )

  // Core systems
  private val gameEngine: GameEngine = new GameEngineImpl()
  private val eventHandler: EventHandler = EventHandler.create(gameEngine)
  private val inputSystem: InputSystem = InputSystem()

  // Mutable state
  private var state: GameSystemsState = GameSystemsState.initial()
  private var isInitialized: Boolean = false

  def initialize(): Unit =
    world.clear()
    state = GameSystemsState.initial()

    if !isInitialized then
      gameEngine.initialize(this)
      setupEventHandlers()
      isInitialized = true
    else
      eventHandler.clearQueue()

    state.render.clearCache()

  def update(): Unit =
    if eventHandler.getCurrentPhase == GamePhase.Playing && !gameEngine.isPaused then
      state = state.updateAll(world)
      ViewController.render()

  def getCurrentElixir: Int = state.getCurrentElixir
  def getRenderSystem: RenderSystem = state.render

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
    state = state.selectWizard(wizardType)
    val wizardCells = getWorld.getEntitiesByType("wizard").flatMap(
      entity => getWorld.getComponent[PositionComponent](entity).map(_.position)
        .map(p => GridMapper.logicalToPhysical(p))).toSeq
    val freeCells = GridMapper.allCells.diff(wizardCells)
    ViewController.drawPlacementGrid(freeCells, wizardCells)

  def handleMouseClick(x: Int, y: Int): Unit =
    val clickResult = inputSystem.handleMouseClick(x, y)
    if clickResult.isValid then
      handleGridClick(clickResult.position)
      ViewController.render()

  def placeWizard(wizardType: WizardType, position: Position): Unit =
    val cost = ShopPanel.getWizardCost(wizardType)
    val isFirstWizard = !state.elixir.firstWizardPlaced
    val elixirBefore = state.getCurrentElixir
    println(s"[DEBUG] Placing wizard - Type: $wizardType, Cost: $cost, Elixir before: $elixirBefore, First wizard: $isFirstWizard")
    val result = for
      _ <- Either.cond(world.getEntityAt(position).isEmpty, (), s"Cell at $position is occupied")
      _ <- Either.cond(state.canAfford(cost), (), s"Insufficient elixir (need $cost, have ${state.getCurrentElixir})")
    yield ()
    result match
      case Right(_) =>
        state.spendElixir(cost) match
          case Some(stateAfterSpending) =>
            println(s"[DEBUG] Elixir after spending: ${stateAfterSpending.getCurrentElixir}")
            val entity = createWizardEntity(wizardType, position)
            state = if isFirstWizard then
              val activatedElixir = stateAfterSpending.elixir.activateGeneration()
              println(s"[DEBUG] First wizard placed, generation activated at ${System.currentTimeMillis()}")
              stateAfterSpending.copy(
                elixir = activatedElixir,
                health = stateAfterSpending.health.copy(elixirSystem = activatedElixir)
              ).clearWizardSelection
            else
              stateAfterSpending.clearWizardSelection
            println(s"[DEBUG] Final elixir: ${state.getCurrentElixir}")
            ViewController.hidePlacementGrid()
            ViewController.render()
          case None =>
            println(s"[ERROR] Failed to spend elixir despite canAfford returning true!")
            ViewController.showError(s"Cannot place ${wizardType.toString}: Failed to spend elixir")
            ViewController.hidePlacementGrid()
      case Left(error) =>
        ViewController.showError(s"Cannot place ${wizardType.toString}: $error")
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
      handleGridClick(event.pos)

  private def handleGridClick(position: Position): Unit =
    state.selectedWizardType match
      case Some(wizardType) =>
        placeWizard(wizardType, position)
      case None =>
        ViewController.showError("No wizard selected. Please select a wizard to place.")
        
      ViewController.hidePlacementGrid()

  private def createWizardEntity(wizardType: WizardType, position: Position): EntityId =
    wizardType match
      case Generator => EntityFactory.createGeneratorWizard(world, position)
      case Barrier => EntityFactory.createBarrierWizard(world, position)
      case Wind => EntityFactory.createWindWizard(world, position)
      case Fire => EntityFactory.createFireWizard(world, position)
      case Ice => EntityFactory.createIceWizard(world, position)

  private def isMenuPhase(phase: GamePhase): Boolean = phase.isMenu || phase == GamePhase.Paused