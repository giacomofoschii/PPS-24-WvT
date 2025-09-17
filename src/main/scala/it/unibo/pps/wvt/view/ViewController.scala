package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.controller.GameController
import it.unibo.pps.wvt.controller.GameEvent._
import it.unibo.pps.wvt.model.CellType._
import it.unibo.pps.wvt.model.{CellType, Grid, Position}
import it.unibo.pps.wvt.utilities.GridMapper
import scalafx.scene._
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.image.Image

object ViewController extends JFXApp3 {
  private var currentGrid: Option[Grid] = None
  private var gameController: Option[GameController] = None

  override def start(): Unit =
    gameController = Some(GameController())
    showMainMenu()

  def showMainMenu(): Unit =
    gameController.get.postEvent(ShowMainMenu)
    stage = createStandardStage(MainMenu())
  
  def showGameView(): Unit =
    gameController.get.postEvent(ShowGameView)
    initializeGrid()
    stage = createStandardStage(GameView())

  def showGameInfo(): Unit = 
    gameController.get.postEvent(ShowInfoMenu)
    stage = createStandardStage(InfoMenu())

  def showGridStatus(grid: Grid): Unit =
    val greenPositions = grid.getAvailablePositions.map(GridMapper.logicalToPhysical)
    val redPositions = grid.getCellsByType(Troll).map(_.position).map(GridMapper.logicalToPhysical)
    GameView.drawGrid(greenPositions, redPositions)

  def hideGridStatus(): Unit =
    GameView.clearGrid()
    
  def render(): Unit = ???

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

  private def initializeGrid(): Unit =
    currentGrid = Some(Grid.empty)
}