package it.unibo.pps.wvt.controller

import scala.collection.mutable
import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.view.ViewController
import it.unibo.pps.wvt.ecs.core.{System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.systems.*
import it.unibo.pps.wvt.utilities.Position


class GameController(world: World) {
  // Core systems
  private val gameEngine: GameEngine = new GameEngineImpl()
  private val eventHandler: EventHandler = EventHandler.create(gameEngine)
  private val inputSystem: InputSystem = InputSystem()

  //ECS Systems
  private val systems = mutable.Buffer[System]()
  private val movementSystem = new MovementSystem
  private val renderSystem = new RenderSystem
  //Game state
  private var playerElixir: Int = 100
  private var selectedWizardType: Option[WizardType] = None
  private var currentWave: Int = 1

  def initialize(): Unit =
    gameEngine.initialize(this)
    systems += movementSystem
    systems += renderSystem
    setupEventHandlers()

  def update(): Unit =
    if(eventHandler.getCurrentPhase == GamePhase.Playing && !gameEngine.isPaused)
      systems.foreach(_.update(world))

  def postEvent(event: GameEvent): Unit =
    eventHandler.postEvent(event)

    event match
      case GameEvent.Pause | GameEvent.Resume | GameEvent.ShowMainMenu |
           GameEvent.ShowGameView | GameEvent.ShowInfoMenu | GameEvent.ExitGame =>
        eventHandler.processEvents()
      case _ =>
        if(isMenuPhase(eventHandler.getCurrentPhase))
          eventHandler.processEvents()

  def start(): Unit = gameEngine.start()
  def stop(): Unit = gameEngine.stop()
  def pause(): Unit = gameEngine.pause()
  def resume(): Unit = gameEngine.resume()

  def getEngine: GameEngine = gameEngine
  def getInputSystem: InputSystem = inputSystem
  def getEventHandler: EventHandler = eventHandler
  def getWorld: World = world
  def getRenderSystem: RenderSystem = renderSystem

  def selectWizard(wizardType: WizardType): Unit = selectedWizardType = Some(wizardType)

  def handleMouseClick(x: Int, y: Int): Unit =
    val clickResult = inputSystem.handleMouseClick(x, y)
    if(clickResult.isValid)
      handleGridClick(clickResult.position)
      ViewController.render()
    else
      ViewController.showError(clickResult.error.get)

  def placeWizard(wizardType: WizardType, position: Position): Unit =
    if (world.getEntityAt(position).isEmpty)
      val entity = wizardType match
        case WizardType.Generator => EntityFactory.createGeneratorWizard(world, position)
        case WizardType.Barrier => EntityFactory.createBarrierWizard(world, position)
        case WizardType.Wind => EntityFactory.createFireWizard(world, position)
        case WizardType.Fire => EntityFactory.createFireWizard(world, position)
        case WizardType.Ice => EntityFactory.createIceWizard(world, position)
        case _ => throw new NotImplementedError("Wizard type not implemented yet")
      ViewController.render()
    else
      ViewController.showError(s"Cannot place ${wizardType.toString} at $position. Cell is occupied.")

  def placeTroll(trollType: TrollType, position: Position): Unit =
    if (world.getEntityAt(position).isEmpty)
      val entity = trollType match
        case TrollType.Base => EntityFactory.createBaseTroll(world, position)
        case TrollType.Warrior => EntityFactory.createWarriorTroll(world, position)
        case TrollType.Assassin => EntityFactory.createAssassinTroll(world, position)
        case TrollType.Thrower => EntityFactory.createThrowerTroll(world, position)
        case _ => throw new NotImplementedError("Troll type not implemented yet")
      ViewController.render()

  private def setupEventHandlers(): Unit = {
    eventHandler.registerHandler(classOf[GameEvent.GridClicked]) { (event: GameEvent.GridClicked) =>
      handleGridClick(event.pos)
    }
  }

  private def handleGridClick(position: Position): Unit =
    selectedWizardType match
      case Some(wizardType) =>
        if (canPlaceWizard(position, wizardType))
          placeWizard(wizardType, position)
          val cost = getWizardCost(wizardType)
          playerElixir -= cost
          selectedWizardType = None
        else
          ViewController.showError(s"Cannot place ${wizardType.toString} at $position. " +
            s"Either the cell is occupied or you lack sufficient elixir.")
      case None =>
        ViewController.showError("No wizard selected. Please select a wizard to place.")

  private def canPlaceWizard(position: Position, wizardType: WizardType): Boolean =
    val cost = getWizardCost(wizardType)
    world.getEntityAt(position).isEmpty && playerElixir >= cost

  private def getWizardCost(wizardType: WizardType): Int = wizardType match
    case WizardType.Generator => 50
    case WizardType.Wind => 70
    case WizardType.Barrier => 60
    case _ => 100
  //implement the other

  private def isMenuPhase(phase: GamePhase): Boolean = phase.isMenu || phase == GamePhase.Paused
}