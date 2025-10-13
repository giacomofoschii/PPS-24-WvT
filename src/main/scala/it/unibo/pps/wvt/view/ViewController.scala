package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.controller.GameEvent.ExitGame
import it.unibo.pps.wvt.controller.{GameController, GameEvent}
import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import scalafx.Includes.jfxScene2sfx
import scalafx.scene.*
import scalafx.application.{JFXApp3, Platform}
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.image.Image

sealed trait ViewState
object ViewState:
  case object MainMenu extends ViewState
  case object GameView extends ViewState
  case object InfoMenu extends ViewState
  case object PauseMenu extends ViewState
  case object Victory extends ViewState
  case object Defeat extends ViewState

case class ViewControllerState(
                              gameController: Option[GameController] = None,
                              currentViewState: ViewState = ViewState.MainMenu,
                              gameViewRoot: Option[Parent] = None,
                              primaryStage: Option[PrimaryStage] = None
                              )

object ViewController extends JFXApp3:
  private var vcState: ViewControllerState = ViewControllerState()

  override def start(): Unit =
    vcState = initializeController(vcState)
    updateView(ViewState.MainMenu)

  override def stopApp(): Unit =
    cleanup()
    vcState.gameController.foreach(_.stop())
    super.stopApp()

  private def initializeController(state: ViewControllerState): ViewControllerState =
    val world = new World()
    val gameController = GameController(world)
    gameController.initialize()
    state.copy(gameController = Some(gameController))

  private def shouldInitializeWorld(previousState: ViewState, newState: ViewState): Boolean =
    (previousState, newState) match
      case (prev, ViewState.GameView) =>
        prev match
          case ViewState.MainMenu => true
          case ViewState.PauseMenu | ViewState.Victory | ViewState.Defeat => false
          case _ => true
      case _  => false

  private def shouldCleanup(previousState: ViewState, newState: ViewState): Boolean =
    (previousState, newState) match
      case (ViewState.PauseMenu | ViewState.GameView |
            ViewState.Victory | ViewState.Defeat, ViewState.MainMenu) => true
      case (ViewState.MainMenu, ViewState.GameView) => true
      case _ => false

  def updateView(viewState: ViewState): Unit =
    val previousState = vcState.currentViewState
    vcState = vcState.copy(currentViewState = viewState)

    Option.when(shouldCleanup(previousState, viewState))(cleanup())

    val (newRoot, updatedState) = createViewForState(viewState, previousState, vcState)
    vcState = updatedState
    
    updateStage(newRoot, viewState)

  private def createViewForState(viewState: ViewState,
                                 previousState: ViewState,
                                 state: ViewControllerState
                                ): (Parent, ViewControllerState) =
    viewState match
      case ViewState.MainMenu =>
        (MainMenu(), state.copy(gameViewRoot = None))

      case ViewState.GameView =>
        createGameView(previousState, state)

      case ViewState.InfoMenu =>
        (InfoMenu(), state)

      case ViewState.PauseMenu =>
        (PauseMenu(), state)

      case ViewState.Victory =>
        (GameResultPanel(GameResultPanel.Victory), state)

      case ViewState.Defeat =>
        (GameResultPanel(GameResultPanel.Defeat), state)

  private def createGameView(previousState: ViewState, 
                             state: ViewControllerState
                            ): (Parent, ViewControllerState) =
    (previousState, state.gameViewRoot) match
      case (ViewState.PauseMenu, Some(existingView)) =>
        (existingView, state)

      case (ViewState.Victory, Some(existingView)) =>
        Platform.runLater(updateShopAndWavePanel())
        (existingView, state)

      case (ViewState.Defeat, Some(existingView)) =>
        (existingView, state)

      case _ =>
        state.gameController.foreach: controller =>
          controller.stop()
          controller.initialize()
        
        val view = GameView()
        render()
        (view, state.copy(gameViewRoot = Some(view)))

  private def updateStage(newRoot: Parent, viewState: ViewState): Unit =
    Platform.runLater:
      vcState.primaryStage match
        case Some(stage) =>
          stage.scene().root = newRoot
        case None =>
          val newStage = createStandardStage(newRoot)
          vcState = vcState.copy(primaryStage = Some(newStage))
          stage = newStage

  private def resizeStageForView(viewState: ViewState): Unit =
    viewState match
      case ViewState.GameView =>
        stage.sizeToScene()
        stage.centerOnScreen()
      case _ =>
        stage.width = Double.NaN
        stage.height = Double.NaN
        stage.sizeToScene()
        stage.centerOnScreen()

  def requestMainMenu(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.ShowMainMenu))

  def requestGameView(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.ShowGameView))

  def requestInfoMenu(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.ShowInfoMenu))

  def requestExitGame(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.ExitGame))

  def requestPauseGame(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.Pause))

  def requestResumeGame(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.Resume))

  def requestContinueBattle(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.ContinueBattle))

  def requestNewGame(): Unit =
    vcState.gameController.foreach(_.postEvent(GameEvent.NewGame))

  def requestPlaceWizard(wizardType: WizardType): Unit =
    vcState.gameController.foreach: controller =>
      controller.postEvent(GameEvent.SelectWizard(wizardType))

  def requestGridClick(x: Double, y: Double): Unit =
    vcState.gameController.foreach: controller =>
      Option.when(controller.getInputSystem.isInGridArea(x, y)):
        for
          logical <- GridMapper.physicalToLogical(Position(x, y))
        yield controller.postEvent(GameEvent.GridClicked(logical, x.toInt, y.toInt))
    
  def getController: Option[GameController] = vcState.gameController
  
  def render(): Unit =
    vcState.gameController.foreach: controller =>
      controller.getRenderSystem.update(controller.getWorld)
      updateShopAndWavePanel()
  
  def drawPlacementGrid(green: Seq[Position], red: Seq[Position]): Unit =
    GameView.drawGrid(green, red)

  def hidePlacementGrid(): Unit =
    GameView.clearGrid()
  
  def showError(message: String): Unit =
    GameView.showError(message)

  private def updateShopAndWavePanel(): Unit =
    Platform.runLater:
      ShopPanel.updateElixir()
      WavePanel.updateWave()

  def cleanupBeforeExit(): Unit =
    cleanup()

  private def cleanup(): Unit =
    vcState.gameController.foreach(_.stop())
    vcState.gameController.foreach(_.getWorld.clear())
    
    GameView.cleanup()
    vcState = vcState.copy(gameViewRoot = None)
    ImageFactory.clearCache()

    Platform.runLater:
      ShopPanel.updateElixir()
      WavePanel.reset()

  private def createStandardStage(pRoot: Parent): PrimaryStage =
    new PrimaryStage:
      title = "Wizards vs Trolls"
      scene = new Scene:
        root = pRoot

      for
        stream <- Option(getClass.getResourceAsStream("/window_logo.png"))
        icon = new Image(stream)
      do icons += icon

      resizable = false
      centerOnScreen()

      onCloseRequest = event =>
        event.consume()
        handleWindowClose()

  private def handleWindowClose(): Unit =
    vcState.gameController.foreach(_.postEvent(ExitGame))