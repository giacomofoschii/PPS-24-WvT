package it.unibo.pps.wvt.controller

import scala.collection.mutable
import it.unibo.pps.wvt.engine.*
import it.unibo.pps.wvt.input.InputSystem
import it.unibo.pps.wvt.view.ViewController
import it.unibo.pps.wvt.ecs.core.{System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.systems.RenderSystem
import it.unibo.pps.wvt.utilities.Position


class GameController(world: World) {
  // Core systems
  private val gameEngine: GameEngine = new GameEngineImpl()
  private val eventHandler: EventHandler = EventHandler.create(gameEngine)
  private val inputSystem: InputSystem = InputSystem()

  //ECS Systems
  private val systems = mutable.Buffer[System]()
  private val renderSystem = new RenderSystem()

  //Game state
  private var playerElixir: Int = 100
  private var selectedWizardType: Option[WizardType] = None
  private var currentWave: Int = 1

  def initialize(): Unit =
    gameEngine.initialize(this)
    systems += renderSystem
    setupEventHandlers()

  def update(): Unit =
    if(eventHandler.getCurrentPhase == GamePhase.Playing && !gameEngine.isPaused)
      systems.foreach(_.update(world))

  def postEvent(event: GameEvent): Unit =
    eventHandler.postEvent(event)

    event match
      case _: GameEvent.Pause.type | _: GameEvent.Resume.type | _: GameEvent.ShowMainMenu.type |
           _: GameEvent.ShowGameView.type | _: GameEvent.ShowInfoMenu.type | _: GameEvent.ExitGame.type =>
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

  private def setupEventHandlers(): Unit =
    eventHandler.registerHandler(classOf[GameEvent.GridClicked], event => {
      val clickEvent = event.asInstanceOf[GameEvent.GridClicked]
      handleGridClick(clickEvent.pos)
    })

  private def isMenuPhase(phase: GamePhase): Boolean = phase match
    case GamePhase.MainMenu | GamePhase.InfoMenu | GamePhase.Paused => true
    case _ => false
}