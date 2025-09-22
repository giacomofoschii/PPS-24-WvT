package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.controller.{GameController, GameEvent}
import it.unibo.pps.wvt.ecs.core.World
import scalafx.Includes.jfxScene2sfx
import scalafx.scene.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.image.Image

sealed trait ViewState
object ViewState {
  case object MainMenu extends ViewState
  case object GameView extends ViewState
  case object InfoMenu extends ViewState
}

object ViewController extends JFXApp3 {
  private val world: World = new World()
  private var gameController: Option[GameController] = None
  private var currentViewState: ViewState = ViewState.MainMenu

  override def start(): Unit =
    gameController = Some(GameController(world))
    gameController.foreach(_.initialize())
    updateView(ViewState.MainMenu)

  def updateView(viewState: ViewState): Unit =
    currentViewState = viewState
    val newRoot = viewState match
      case ViewState.MainMenu => MainMenu()
      case ViewState.GameView =>
        initializeWorld()
        val view = GameView()
        render()
        view
      case ViewState.InfoMenu => InfoMenu()

    if(viewState == ViewState.GameView || stage == null)
      stage = createStandardStage(newRoot)
    else
      stage.scene().root = newRoot


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

  def hideGridStatus(): Unit = GameView.clearGrid()
  def render(): Unit = gameController.foreach(_.getRenderSystem.update(world))
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
    }

  private def initializeWorld(): Unit =
    world.clear()
  //possibilità utente di aggiungere le prime entità? Si parte già con un generator?
}