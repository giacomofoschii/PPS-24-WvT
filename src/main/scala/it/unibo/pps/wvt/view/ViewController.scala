package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.controller.GameEvent.ExitGame
import it.unibo.pps.wvt.controller.{GameController, GameEvent}
import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.GridMapper.PhysicalCoords
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
    gameController.foreach: controller =>
      controller.stop()
    super.stopApp()

  def updateView(viewState: ViewState): Unit =
    val previousState = currentViewState
    currentViewState = viewState
    val newRoot = viewState match
      case ViewState.MainMenu =>
        if previousState == ViewState.PauseMenu || previousState == ViewState.GameView then
          cleanupGame()
        gameViewRoot = None
        MainMenu()
      case ViewState.GameView =>
        if previousState == ViewState.MainMenu then
          cleanupGame()
          initializeWorld()
          val view = GameView()
          gameViewRoot = Some(view)
          render()
          view
        else if previousState == ViewState.PauseMenu && gameViewRoot.isDefined then
          gameViewRoot.get
        else
          // Fallback: create new game
          initializeWorld()
          val view = GameView()
          gameViewRoot = Some(view)
          render()
          view
      case ViewState.InfoMenu => InfoMenu()
      case ViewState.PauseMenu => PauseMenu()

    Platform.runLater:
      if stage != null then
        stage.scene().root = newRoot
        viewState match
          case ViewState.GameView =>
            stage.sizeToScene()
            stage.centerOnScreen()
          case _ =>
            stage.width = Double.NaN
            stage.height = Double.NaN
            stage.sizeToScene()
            stage.centerOnScreen()
      else
        stage = createStandardStage(newRoot)

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
    
  def requestPlaceWizard(wizardType: WizardType): Unit =
    ViewController.getController.foreach(_.selectWizard(wizardType))

  private def updateShopElixir(): Unit =
    gameController.foreach: controller =>
      ShopPanel.updateElixir()

  def drawPlacementGrid(green: Seq[PhysicalCoords], red: Seq[PhysicalCoords]): Unit = GameView.drawGrid(green, red)
  def hidePlacementGrid(): Unit = GameView.clearGrid()

  def render(): Unit =
    gameController.foreach(_.getRenderSystem.update(world))
    updateShopElixir()

  def getWorld: World = world
  def getController: Option[GameController] = gameController
  def showError(message: String): Unit = GameView.showError(message)

  private def createStandardStage(pRoot: Parent): PrimaryStage =
    new PrimaryStage {
      title = "Wizards vs Trolls"
      scene = new Scene {
        root = pRoot
      }

      Option(getClass.getResourceAsStream("/window_logo.png"))
        .map(new Image(_))
        .foreach(icon => icons += icon)

      resizable = false
      centerOnScreen()

      onCloseRequest = event =>
        event.consume()
        handleWindowClose()
    }

  private def handleWindowClose(): Unit =
    gameController.foreach(_.postEvent(ExitGame))

  private def initializeWorld(): Unit =
    world.clear()
    gameController.foreach: controller =>
      controller.stop()
      controller.initialize()
  //possibilità utente di aggiungere le prime entità? Si parte già con un generator?

  def cleanupBeforeExit(): Unit =
    cleanupGame()

  private def cleanupGame(): Unit =
    gameController.foreach(_.stop())
    world.clear()
    GameView.cleanup()
    gameViewRoot = None
    ImageFactory.clearCache()
    ShopPanel.updateElixir()