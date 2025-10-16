package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.controller.GameEvent.*
import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.engine.GamePhase.{Paused, Playing}
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

/** The GameController class manages the game state, processes events, and coordinates between the game engine,
  * event handler, input system, and the game world.
  *
  * @param world The initial game world.
  */
class GameController(private var world: World):

  private type StateTransformation = GameSystemsState => GameSystemsState

  private val gameEngine: GameEngine                                     = new GameEngineImpl()
  private val eventHandler: EventHandler                                 = createAndSetupEventHandler()
  private val inputSystem: InputSystem                                   = InputSystem()
  private val pendingActions: ConcurrentLinkedQueue[StateTransformation] = new ConcurrentLinkedQueue()

  private var state: GameSystemsState = GameSystemsState.initial()
  private var isInitialized: Boolean  = false

  /** Creates and sets up the event handler with necessary event registrations.
    *
    * @return The configured EventHandler instance.
    */
  private def createAndSetupEventHandler(): EventHandler =
    val handler = EventHandler.create(gameEngine)

    handler.registerHandler(classOf[GameEvent.GridClicked]): (event: GameEvent.GridClicked) =>
      GridMapper.logicalToPhysical(event.logicalPos)
        .foreach(handleGridClick)

    handler

  /** Initializes the game controller, setting up the world and state.
    * This method is idempotent and will only initialize the game engine once.
    */
  def initialize(): Unit =
    world = World.empty
    state = GameSystemsState.initial()
    pendingActions.clear()

    Option.when(!isInitialized):
      gameEngine.initialize(this)
      isInitialized = true
    .getOrElse:
      eventHandler.clearQueue()

  /** Processes all pending state transformations in a thread-safe manner. */
  @tailrec
  private def processPendingActions(): Unit =
    Option(pendingActions.poll()) match
      case Some(action) =>
        state = action(state)
        processPendingActions()
      case None => ()

  /** Updates the game state and world. This method is synchronized to ensure thread safety.
    * It processes pending actions, updates the world and state, and handles wave changes.
    */
  def update(): Unit =
    synchronized:
      Option.when(
        eventHandler.getCurrentPhase == Playing &&
          !gameEngine.isPaused
      ):
        processPendingActions()

        val oldWave              = state.getCurrentWave
        val (newWorld, newState) = state.updateAll(world)

        world = newWorld
        state = newState

        state.selectedWizardType.foreach(_ => repaintGrid())

        Option.when(oldWave != state.getCurrentWave):
          notifyWaveChange(state.getCurrentWave)

        ViewController.render()

  /** Posts a game event to the event handler and processes events if necessary.
    *
    * @param event The game event to be posted.
    */
  def postEvent(event: GameEvent): Unit =
    eventHandler.postEvent(event)

    event match
      case Pause | Resume | ShowMainMenu | ShowGameView | ShowInfoMenu | ExitGame =>
        eventHandler.processEvents()
      case _ =>
        Option.when(isMenuPhase(eventHandler.getCurrentPhase)):
          eventHandler.processEvents()

  /** Handles mouse click events by translating physical coordinates to logical grid positions
    * and processing the grid click if valid.
    *
    * @param x The x-coordinate of the mouse click.
    * @param y The y-coordinate of the mouse click.
    */
  def handleMouseClick(x: Double, y: Double): Unit =
    inputSystem.handleMouseClick(x, y) match
      case result if result.isValid =>
        val processedPos =
          for
            logical  <- GridMapper.physicalToLogical(result.pos)
            physical <- GridMapper.logicalToPhysical(logical)
          yield physical

        processedPos.foreach(handleGridClick)
        ViewController.render()
      case _ => ()

  /** Handles a grid click event at the specified position.
    * If a wizard type is selected, it attempts to place the wizard at the given position.
    * If no wizard type is selected, it shows an error message.
    *
    * @param position The position where the grid was clicked.
    */
  private def handleGridClick(position: Position): Unit =
    state.selectedWizardType match
      case Some(wizardType) =>
        placeWizard(wizardType, position)
      case None =>
        ViewController.showError("No wizard selected for placement, please select one from the shop first.")
        ViewController.hidePlacementGrid()

  /** Attempts to place a wizard of the specified type at the given position.
    * It checks for sufficient resources and cell occupancy before placing the wizard.
    * If placement is successful, it updates the game state and hides the placement grid.
    * If placement fails, it shows an appropriate error message.
    *
    * @param wizardType The type of wizard to place.
    * @param position The position where the wizard should be placed.
    */
  def placeWizard(wizardType: WizardType, position: Position): Unit =
    val cost     = ShopPanel.getWizardCost(wizardType)
    val canPlace = Option.when(!world.hasWizardAt(position) && state.canAfford(cost))(())

    canPlace match
      case Some(_) =>
        val (newWorld, _) = createWizardEntity(wizardType, position)
        world = newWorld

        val isFirstWizard = !state.elixir.firstWizardPlaced
        val transformation: StateTransformation = currentState =>
          currentState.spendElixir(cost).map { stateAfterSpending =>
            Option.when(isFirstWizard)(stateAfterSpending).fold(
              stateAfterSpending.clearWizardSelection
            ) { _ =>
              val activatedElixir = stateAfterSpending.elixir.activateGeneration()
              stateAfterSpending.copy(
                elixir = activatedElixir,
                health = stateAfterSpending.health.copy(elixirSystem = activatedElixir)
              ).clearWizardSelection
            }
          }.getOrElse(currentState.clearWizardSelection)
        pendingActions.add(transformation)
        ViewController.hidePlacementGrid()
      case None =>
        val errorMessage = world.getEntityAt(position)
          .map(_ => s"Cannot place $wizardType: cell occupied.")
          .getOrElse(s"Cannot place $wizardType: insufficient elixir.")
        ViewController.showError(errorMessage)
        ViewController.hidePlacementGrid()

  /** Selects a wizard type for placement and updates the placement grid accordingly.
    *
    * @param wizardType The type of wizard to select.
    */
  def selectWizard(wizardType: WizardType): Unit =
    pendingActions.add: currentState =>
      currentState.selectedWizardType
        .filter(_ == wizardType)
        .fold(currentState.selectWizard(wizardType))(_ => currentState)

    repaintGrid()

  /** Deselects any currently selected wizard type and hides the placement grid. */
  private def repaintGrid(): Unit =
    val occupiedCells = calculateOccupiedCells()
    val freeCells     = GridMapper.allCells.diff(occupiedCells)
    ViewController.drawPlacementGrid(freeCells, occupiedCells)

  /** Calculates the positions of all occupied cells in the grid by checking for existing wizard entities.
    *
    * @return A sequence of positions representing occupied cells.
    */
  private def calculateOccupiedCells(): Seq[Position] =
    world.getEntitiesByType("wizard")
      .flatMap: entity =>
        for
          posComp       <- world.getComponent[PositionComponent](entity)
          logicalCoords <- GridMapper.physicalToLogical(posComp.position)
        yield Position(
          GRID_OFFSET_X + logicalCoords._2 * CELL_WIDTH,
          GRID_OFFSET_Y + logicalCoords._1 * CELL_HEIGHT
        )
      .toSeq

  /** Creates a wizard entity of the specified type at the given position and adds it to the world.
    *
    * @param wizardType The type of wizard to create.
    * @param position The position where the wizard should be created.
    * @return A tuple containing the updated world and the ID of the created entity.
    */
  private def createWizardEntity(wizardType: WizardType, position: Position): (World, EntityId) =
    wizardType match
      case Generator => EntityFactory.createGeneratorWizard(world, position)
      case Barrier   => EntityFactory.createBarrierWizard(world, position)
      case Wind      => EntityFactory.createWindWizard(world, position)
      case Fire      => EntityFactory.createFireWizard(world, position)
      case Ice       => EntityFactory.createIceWizard(world, position)

  /** Handles the continuation of a battle after a victory.
    * It resets the world and state, clears pending actions, and resumes the game engine if it was running.
    * The placement grid is hidden and the view is re-rendered.
    */
  def handleContinueBattle(): Unit =
    Option.when(gameEngine.isRunning):
      gameEngine.resume()

    Thread.sleep(50)

    synchronized:
      world = World.empty
      state = state.handleVictory()
      pendingActions.clear()

    ViewController.hidePlacementGrid()
    ViewController.render()

    Option.when(gameEngine.isRunning):
      gameEngine.start()

  /** Handles the initiation of a new game.
    * It stops the game engine if it was running, resets the world and state,
    * clears pending actions, and initializes the game engine.
    * The placement grid is hidden and the view is re-rendered.
    */
  def handleNewGame(): Unit =
    Option.when(gameEngine.isRunning):
      gameEngine.stop()

    Thread.sleep(50)

    synchronized:
      world = World.empty
      state = state.reset()
      pendingActions.clear()
      isInitialized = false

    initialize()
    notifyWaveChange(state.getCurrentWave)
    ViewController.hidePlacementGrid()
    ViewController.render()

  /** Notifies the wave panel of a change in the current wave number.
    * This method ensures that the update occurs on the JavaFX Application Thread.
    *
    * @param newWave The new wave number to be displayed.
    */
  private def notifyWaveChange(newWave: Int): Unit =
    Platform.runLater(WavePanel.updateWaveNumber(newWave))

  /** Checks if the given game phase is a menu phase (either a menu or paused).
    *
    * @param phase The game phase to check.
    * @return True if the phase is a menu phase, false otherwise.
    */
  private def isMenuPhase(phase: GamePhase): Boolean =
    phase.isMenu || phase == Paused

  // Accessors
  def getCurrentElixir: Int         = state.getCurrentElixir
  def getRenderSystem: RenderSystem = state.render
  def getCurrentWaveInfo: (Int, Int, Int) =
    (state.getCurrentWave, state.getTrollsSpawned, state.getMaxTrolls)

  def start(): Unit  = gameEngine.start()
  def stop(): Unit   = gameEngine.stop()
  def pause(): Unit  = gameEngine.pause()
  def resume(): Unit = gameEngine.resume()

  def getEngine: GameEngine         = gameEngine
  def getInputSystem: InputSystem   = inputSystem
  def getEventHandler: EventHandler = eventHandler
  def getWorld: World               = world
