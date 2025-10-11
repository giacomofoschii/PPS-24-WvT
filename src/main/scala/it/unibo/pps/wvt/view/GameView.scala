package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.scene.Parent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{Pane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.application.Platform

import scala.annotation.tailrec

case class GameViewState(
                        entities: Seq[(Position, String)] = Seq.empty,
                        healthBars: Seq[(Position, Double, Color, Double, Double, Double)] = Seq.empty,
                        gridCells: (Seq[Position], Seq[Position]) = (Seq.empty, Seq.empty)
                        )

private case class RenderablePanes(
                                  grid: Pane,
                                  entities: Pane,
                                  projectiles: Pane,
                                  healthBars: Pane,
                                  ui: Pane,
                                  stack: StackPane
                                  )

object GameView:
  private var panes: Option[RenderablePanes] = None

  def apply(): Parent =
    cleanup()

    lazy val backgroundImage =
      createBackgroundView("/background_grid.png", GAME_MAP_SCALE_FACTOR).getOrElse(new ImageView())

    val gridOverlay = new Pane()
    gridOverlay.mouseTransparent = true

    val entityOverlay = new Pane()

    val projectileOverlay = new Pane()
    projectileOverlay.mouseTransparent = true

    val uiOverlay = createUIOverlay

    val healthBarOverlay = new Pane()
    healthBarOverlay.mouseTransparent = true

    val stackPane = new StackPane:
      children = Seq(backgroundImage, gridOverlay, entityOverlay, projectileOverlay, uiOverlay, healthBarOverlay)
      onMouseClicked = event =>
        handleMouseClick(event.getX, event.getY)

    panes = Some(RenderablePanes(
      gridOverlay,
      entityOverlay,
      projectileOverlay,
      healthBarOverlay,
      uiOverlay,
      stackPane
    ))

    stackPane

  def renderEntities(entities: Seq[(Position, String)]): Unit =
    Platform.runLater:
      panes.foreach: p =>
        val (projectiles, others) = entities.partition { case (_, path) => path.contains("/projectile/") }

        renderEntitiesInPane(p.entities, others)
        renderEntitiesInPane(p.projectiles, projectiles)

  def renderHealthBars(healthBars: Seq[(Position, Double, Color, Double, Double, Double)]): Unit =
    Platform.runLater:
      panes.foreach: p =>
        p.healthBars.children.clear()
        healthBars.foreach(renderSingleHealthBar(p.healthBars))


  def drawGrid(greenCells: Seq[Position], redCells: Seq[Position]): Unit =
    Platform.runLater:
      panes.foreach: p =>
        p.grid.children.clear()
  
        @tailrec
        def drawCells(cells: List[Position], color: Color): Unit =
          cells match
            case Nil => ()
            case Position(x, y) :: tail =>
              p.grid.children.add(createStatusCell(x, y, color))
              drawCells(tail, color)
  
        drawCells(greenCells.toList, Color.Green)
        drawCells(redCells.toList, Color.Red)


  def clearGrid(): Unit =
    Platform.runLater:
      panes.foreach(_.grid.children.clear())

  def showError(message: String) : Unit =
    Platform.runLater:
      val alert = new Alert(AlertType.Error):
        title = "Error"
        headerText = "An error occurred"
        contentText = message
      alert.showAndWait()

  private def handleMouseClick(x: Double, y: Double): Unit =
    val isOnShop = x >= 10 && x <= 260 && y >= 10
    if !isOnShop then
      ViewController.requestGridClick(x, y)

  private def renderEntitiesInPane(pane: Pane, entities: Seq[(Position, String)]): Unit =
    pane.children.clear()

    @tailrec
    def addEntities(remaining: List[(Position, String)]): Unit =
      remaining match
        case Nil => ()
        case (Position(centerX, centerY), spritePath) :: tail =>
          createImageView(spritePath, CELL_WIDTH) match
            case Right(imageView) =>
              imageView.preserveRatio = true
              imageView.x = centerX - 75 / 2.0
              imageView.y = centerY - 90 / 2.0
              pane.children.add(imageView)
            case Left(error) =>
              println(s"Error loading image: $error")
          addEntities(tail)

    addEntities(entities.toList)

  private def renderSingleHealthBar(pane: Pane)(data: (Position, Double, Color, Double, Double, Double)): Unit =
    val (Position(centerX, centerY), healthPercent, color, barWidth, barHeight, offsetY) = data

    val backgroundBar = new Rectangle:
      x = centerX - barWidth / 2.0
      y = centerY + offsetY
      width = barWidth
      height = barHeight
      fill = Color.DarkGray
      stroke = Color.Black
      strokeWidth = 0.5

    val healthBar = new Rectangle:
      x = centerX - barWidth / 2.0
      y = centerY + offsetY
      width = barWidth * healthPercent
      height = barHeight
      fill = color

    pane.children.addAll(backgroundBar, healthBar)

  private def createStatusCell(myX: Double, myY: Double, color: Color): Rectangle =
    new Rectangle:
      x = myX
      y = myY
      width = CELL_WIDTH
      height = CELL_HEIGHT
      fill = color
      opacity = CELL_OPACITY
      stroke = Color.White

  private def createUIOverlay: Pane =
    val buttonConfigs = Map(
      "pause" -> ButtonConfig("Pause", 200, 100, 20, "Times New Roman"),
    )
    val pauseButton = createStyledButton(buttonConfigs("pause"))(handleAction(PauseGame))
    val shopPanel = ShopPanel.createShopPanel()
    val shopButton = ShopPanel.createShopButton()
    val wavePanel = WavePanel.createWavePanel()
    val overlayPane = new Pane {
      children = Seq(shopPanel, pauseButton, shopButton, wavePanel)
    }
    shopPanel.layoutX = 10
    shopPanel.layoutY = 10
    shopPanel.prefHeight <== overlayPane.height - 20
    shopPanel.maxHeight <== overlayPane.height - 20
    shopPanel.onMouseClicked = event => event.consume()
    shopPanel.mouseTransparent = false
    shopButton.layoutX = shopPanel.layoutX.value + (250 - 200) / 2
    shopButton.layoutY = 30
    wavePanel.layoutX = 820
    wavePanel.layoutY = 50
    wavePanel.onMouseClicked = event => event.consume()
    wavePanel.mouseTransparent = true
    pauseButton.layoutX = 1050
    pauseButton.layoutY = 30
    overlayPane

  def cleanup(): Unit =
    panes = None
    WavePanel.updateWave()