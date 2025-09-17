package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.utilities.GridMapper.PhysicalCoords
import scalafx.geometry.Insets
import scalafx.scene.Parent
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, Pane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object GameView {
  private var gridPane: Option[Pane] = None
  private var entityPane: Option[Pane] = None

  def apply(): Parent =
    lazy val backgroundImage =
      createBackgroundView("/background_grid.png", GAME_MAP_SCALE_FACTOR).getOrElse(new ImageView())

    val gridOverlay = new Pane()
    gridOverlay.mouseTransparent = true

    val entityOverlay = new Pane()

    val buttonOverlay = createButtonOverlay

    val stackPane = new StackPane {
      children = Seq(backgroundImage, gridOverlay, entityOverlay, buttonOverlay)
    }

    entityPane = Some(entityOverlay)
    gridPane = Some(gridOverlay)
    stackPane

  def drawGrid(greenCells: Seq[PhysicalCoords], redCells: Seq[PhysicalCoords]): Unit =
    gridPane.foreach { pane =>
      pane.children.clear()

      greenCells.foreach { case (x, y) =>
        pane.children.add(createStatusCell(x, y, Color.Green))
      }

      redCells.foreach { case (x, y) =>
        pane.children.add(createStatusCell(x, y, Color.Red))
      }
    }

  def clearGrid(): Unit =
    gridPane.foreach(_.children.clear())

  private def createStatusCell(myX: Double, myY: Double, color: Color): Rectangle =
    new Rectangle {
      x = myX
      y = myY
      width = CELL_WIDTH
      height = CELL_HEIGHT
      fill = color
      opacity = CELL_OPACITY
      stroke = Color.White
    }
    

  private def createButtonOverlay: Pane =
    val buttonConfigs = Map(
      "pause" -> ButtonConfig("Pause", 200, 100, 20, "Times New Roman"),
    )
    val pauseButton = createStyledButton(buttonConfigs("pause"))(handleAction(PauseGame))

    new BorderPane {
      top = new BorderPane {
        padding = Insets(5)
        right = pauseButton
      }
    }
}