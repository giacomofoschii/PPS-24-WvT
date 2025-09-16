package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.model.CellType._
import it.unibo.pps.wvt.model.{Cell, CellType, Grid, Position}
import it.unibo.pps.wvt.utilities.GridMapper
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.scene.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.image.Image

object ViewController extends JFXApp3 {
  private var currentGrid: Option[Grid] = None

  override def start(): Unit =
    showMainMenu()
  
  def showMainMenu(): Unit =
    stage = createStandardStage(MainMenu())

  def showGameView(): Unit = {
    stage = createStandardStage(GameView())
    initializeGrid()
  }

  def showGameInfo(): Unit = ???

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
    currentGrid = Some(Grid(Array.tabulate(GRID_ROWS, GRID_COLS) { (row, col) =>
      Cell(Position(row, col), CellType.Empty)
    }))
}
