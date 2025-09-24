package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.view.ViewController
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.WizardType.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.systems.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.Position

class GameController(world: World):

  case class GameSystemsState(
                               movement: MovementSystem,
                               combat: CombatSystem,
                               elixir: ElixirSystem,
                               health: HealthSystem,
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
      val updatedRender = render.update(world).asInstanceOf[RenderSystem]

      copy(
        movement = updatedMovement,
        combat = updatedCombat,
        elixir = updatedElixir,
        health = updatedHealth,
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

    def getCurrentElixir: Int = elixir.getCurrentElixir

    def canAfford(cost: Int): Boolean = elixir.canAfford(cost)

  object GameSystemsState:
    def initial(): GameSystemsState =
      val elixir = ElixirSystem()
      GameSystemsState(
        movement = MovementSystem(),
        combat = CombatSystem(),
        elixir = elixir,
        health = HealthSystem(elixir, Set.empty),
        render = RenderSystem()
      )

  // Core systems
  private val gameEngine: GameEngine = new GameEngineImpl()
  private val eventHandler: EventHandler = EventHandler.create(gameEngine)
  private val inputSystem: InputSystem = InputSystem()

  // Mutable state
  private var state: GameSystemsState = GameSystemsState.initial()

  def initialize(): Unit =
    gameEngine.initialize(this)
    setupEventHandlers()

  def update(): Unit =
    if eventHandler.getCurrentPhase == GamePhase.Playing && !gameEngine.isPaused then
      state = state.updateAll(world)

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

  def handleMouseClick(x: Int, y: Int): Unit =
    val clickResult = inputSystem.handleMouseClick(x, y)
    if clickResult.isValid then
      handleGridClick(clickResult.position)
      ViewController.render()
    else
      ViewController.showError(clickResult.error.get)

  def placeWizard(wizardType: WizardType, position: Position): Unit =
    val cost = getWizardCost(wizardType)
    val result = for
      _ <- Either.cond(world.getEntityAt(position).isEmpty, (), s"Cell at $position is occupied")
      _ <- Either.cond(state.canAfford(cost), (), s"Insufficient elixir (need $cost, have ${state.getCurrentElixir})")
    yield
      val entity = createWizardEntity(wizardType, position)
      state.spendElixir(cost).foreach: newState =>
        state = newState.clearWizardSelection
      ViewController.render()

    result.left.foreach: error =>
      ViewController.showError(s"Cannot place ${wizardType.toString}: $error")

  def placeTroll(trollType: TrollType, position: Position): Unit =
    if world.getEntityAt(position).isEmpty then
      val entity = trollType match
        case TrollType.Base => EntityFactory.createBaseTroll(world, position)
        case TrollType.Warrior => EntityFactory.createWarriorTroll(world, position)
        case TrollType.Assassin => EntityFactory.createAssassinTroll(world, position)
        case TrollType.Thrower => EntityFactory.createThrowerTroll(world, position)
      ViewController.render()
    else
      ViewController.showError(s"Cannot place ${trollType.toString} at $position. Cell is occupied.")

  private def setupEventHandlers(): Unit =
    eventHandler.registerHandler(classOf[GameEvent.GridClicked]): (event: GameEvent.GridClicked) =>
      handleGridClick(event.pos)

  private def handleGridClick(position: Position): Unit =
    state.selectedWizardType match
      case Some(wizardType) =>
        placeWizard(wizardType, position)
      case None =>
        ViewController.showError("No wizard selected. Please select a wizard to place.")

  private def createWizardEntity(wizardType: WizardType, position: Position): EntityId =
    wizardType match
      case Generator => EntityFactory.createGeneratorWizard(world, position)
      case Barrier => EntityFactory.createBarrierWizard(world, position)
      case Wind => EntityFactory.createWindWizard(world, position)
      case Fire => EntityFactory.createFireWizard(world, position)
      case Ice => EntityFactory.createIceWizard(world, position)

  private def getWizardCost(wizardType: WizardType): Int = wizardType match
    case Generator => GENERATOR_WIZARD_COST
    case Barrier => BARRIER_WIZARD_COST
    case Wind => WIND_WIZARD_COST
    case Fire => FIRE_WIZARD_COST
    case Ice => ICE_WIZARD_COST

  private def isMenuPhase(phase: GamePhase): Boolean = phase.isMenu || phase == GamePhase.Paused