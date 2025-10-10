package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.controller.GameEvent.ExitGame
import it.unibo.pps.wvt.controller.{GameController, GameEvent}
import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.Position
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

object ViewController extends JFXApp3:
  private val world: World = new World()
  private var gameController: Option[GameController] = None
  private var currentViewState: ViewState = ViewState.MainMenu
  private var gameViewRoot: Option[Parent] = None

  override def start(): Unit =
    gameController = Some(GameController(world))
    gameController.foreach(_.initialize())
    updateView(ViewState.MainMenu)

  override def stopApp(): Unit =
    cleanupGame()
    gameController.foreach(_.stop())
    super.stopApp()

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
    val previousState = currentViewState
    currentViewState = viewState

    if shouldCleanup(previousState, viewState) then
      cleanupGame()

    val newRoot = createViewForState(viewState, previousState)
    updateStage(newRoot, viewState)

  private def createViewForState(
                                  viewState: ViewState,
                                  previousState: ViewState
                                ): Parent =
    viewState match
      case ViewState.MainMenu =>
        gameViewRoot = None
        MainMenu()

      case ViewState.GameView =>
        createGameView(previousState)

      case ViewState.InfoMenu =>
        InfoMenu()

      case ViewState.PauseMenu =>
        PauseMenu()

      case ViewState.Victory =>
        GameResultPanel(GameResultPanel.Victory)

      case ViewState.Defeat =>
        GameResultPanel(GameResultPanel.Defeat)

  private def createGameView(previousState: ViewState): Parent =
    (previousState, gameViewRoot) match
      case (ViewState.PauseMenu, Some(existingView)) =>
        existingView

      case (ViewState.Victory, Some(existingView)) =>
        Platform.runLater {
          updateShopAndWavePanel()
        }
        existingView

      case (ViewState.Defeat, Some(existingView)) =>
        existingView

      case _ =>
        initializeWorld()
        val view = GameView()
        gameViewRoot = Some(view)
        render()
        view

  private def updateStage(newRoot: Parent, viewState: ViewState): Unit =
    Platform.runLater:
      if stage != null then
        stage.scene().root = newRoot
        resizeStageForView(viewState)
      else
        stage = createStandardStage(newRoot)

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

  // Request functions
  def requestMainMenu(): Unit =
    gameController.foreach(_.postEvent(GameEvent.ShowMainMenu))

  def requestGameView(): Unit =
    gameController.foreach(_.postEvent(GameEvent.ShowGameView))

  def requestInfoMenu(): Unit =
    gameController.foreach(_.postEvent(GameEvent.ShowInfoMenu))

  def requestExitGame(): Unit =
    gameController.foreach(_.postEvent(GameEvent.ExitGame))

  def requestPauseGame(): Unit =
    gameController.foreach(_.postEvent(GameEvent.Pause))

  def requestResumeGame(): Unit =
    gameController.foreach(_.postEvent(GameEvent.Resume))

  def requestContinueBattle(): Unit =
    gameController.foreach(_.postEvent(GameEvent.ContinueBattle))

  def requestNewGame(): Unit =
    gameController.foreach(_.postEvent(GameEvent.NewGame))

  def requestPlaceWizard(wizardType: WizardType): Unit =
    gameController.foreach(_.selectWizard(wizardType))

  private def updateShopAndWavePanel(): Unit =
    Platform.runLater:
      ShopPanel.updateElixir()
      WavePanel.updateWave()

  def drawPlacementGrid(green: Seq[Position], red: Seq[Position]): Unit =
    GameView.drawGrid(green, red)

  def hidePlacementGrid(): Unit =
    GameView.clearGrid()

  def render(): Unit =
    gameController.foreach: controller =>
      controller.getRenderSystem.update(world)
      updateShopAndWavePanel()

  def getWorld: World = world
  def getController: Option[GameController] = gameController

  def showError(message: String): Unit =
    GameView.showError(message)

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
    gameController.foreach(_.postEvent(ExitGame))

  private def initializeWorld(): Unit =
    world.clear()
    gameController.foreach: controller =>
      controller.stop()
      controller.initialize()

  def cleanupBeforeExit(): Unit =
    cleanupGame()

  private def cleanupGame(): Unit =
    gameController.foreach(_.stop())
    world.clear()
    GameView.cleanup()
    gameViewRoot = None
    ImageFactory.clearCache()

    Platform.runLater:
      ShopPanel.updateElixir()
      WavePanel.reset()