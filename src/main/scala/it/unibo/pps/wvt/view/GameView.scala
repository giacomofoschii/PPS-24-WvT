package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.scene.Parent
import scalafx.scene.image.ImageView
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*
import it.unibo.pps.wvt.model.Position
import scalafx.scene.layout.{Pane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object GameView {
  def apply(): Parent = {
    lazy val backgroundImage = 
      createBackgroundView("/background_grid.png", GAME_MAP_SCALE_FACTOR).getOrElse(new ImageView())

    /*
    val overlayPane = new Pane()
    showGrid(overlayPane)
    */
    new StackPane {
      children = Seq(backgroundImage)
    }
  }

  /*
  mi torna utile per quando dovremo fare il posizionamento, possiamo usarlo con posizioni libere o occupate
   */
  def showGrid(pane: Pane): Unit = {
    for {
      row <- 0 until GRID_ROWS
      col <- 0 until GRID_COLS
    } {
      val pos = Position(row, col)
      val (myX, myY) = GridMapper.logicalToPhysical(pos)

      val cellRect = new Rectangle {
        x = myX
        y = myY
        width = CELL_WIDTH
        height = CELL_HEIGHT
        fill = Color.Red
        opacity = 0.4
        stroke = Color.White
        strokeWidth = 1.0
      }

      pane.children.add(cellRect)
    }
  }
}