package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.utilities.GridMapper.PhysicalCoords
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

object GameView:
  private var gridPane: Option[Pane] = None
  private var entityPane: Option[Pane] = None
  private var projectilePane: Option[Pane] = None
  private var wizardButtons: Map[WizardType, Button] = Map.empty
  private var gameStackPane: Option[StackPane] = None
  private var healthBarPane: Option[Pane] = None

  def apply(): Parent =
    gridPane = None
    entityPane = None
    projectilePane = None
    gameStackPane = None
    wizardButtons = Map.empty

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
        val clickX = event.getX
        val clickY = event.getY
        val isOnShop = clickX >= 10 && clickX <= 260 && clickY >= 10
        if !isOnShop then
          handleGridClick(clickX, clickY)

    healthBarPane = Some(healthBarOverlay)
    entityPane = Some(entityOverlay)
    projectilePane = Some(projectileOverlay)
    gridPane = Some(gridOverlay)
    gameStackPane = Some(stackPane)

    stackPane

  def drawGrid(greenCells: Seq[PhysicalCoords], redCells: Seq[PhysicalCoords]): Unit =
    Platform.runLater:
      gridPane.foreach: pane =>
        pane.children.clear()

        @tailrec
        def drawCells(cells: List[PhysicalCoords], color: Color): Unit =
          cells match
            case Nil => ()
            case (x, y) :: tail =>
              pane.children.add(createStatusCell(x, y, color))
              drawCells(tail, color)

        drawCells(greenCells.toList, Color.Green)
        drawCells(redCells.toList, Color.Red)


  def clearGrid(): Unit =
    Platform.runLater:
      gridPane.foreach(_.children.clear())

  def renderEntities(entities: Seq[(PhysicalCoords, String)]): Unit =
    Platform.runLater:
      val (projectiles, others) = entities.partition { case (_, path) => path.contains("/projectile/") }
      entityPane.foreach: pane =>
        pane.children.clear()
        pane.children.addAll(createEntitiesPane(others).children)
      projectilePane.foreach: pane =>
        pane.children.clear()
        pane.children.addAll(createEntitiesPane(projectiles).children)

  def renderHealthBars(healthBars: Seq[(PhysicalCoords, Double, Color, Double, Double, Double)]): Unit =
    Platform.runLater:
      healthBarPane.foreach: pane =>
        pane.children.clear()

        @tailrec
        def renderBars(bars: List[(PhysicalCoords, Double, Color, Double, Double, Double)]): Unit =
          bars match
            case Nil => ()
            case head :: tail =>
              renderSingleHealthBar(pane)(head)
              renderBars(tail)

        renderBars(healthBars.toList)

  def showError(message: String) : Unit =
    Platform.runLater:
      val alert = new Alert(AlertType.Error):
        title = "Error"
        headerText = "An error occurred"
        contentText = message
      alert.showAndWait()

  private def handleGridClick(x: Double, y: Double): Unit =
    ViewController.getController match
      case Some(controller) =>
        if !controller.getEngine.isPaused then
          if controller.getInputSystem.isInGridArea(x.toInt, y.toInt) then
            controller.handleMouseClick(x.toInt, y.toInt)
      case None =>

  private def createStatusCell(myX: Double, myY: Double, color: Color): Rectangle =
    new Rectangle:
      x = myX
      y = myY
      width = CELL_WIDTH
      height = CELL_HEIGHT
      fill = color
      opacity = CELL_OPACITY
      stroke = Color.White

  private def createEntitiesPane(entities: Seq[(PhysicalCoords, String)]): Pane =
    val pane = new Pane()
    pane.children.clear()

    @tailrec
    def addEntities(remaining: List[(PhysicalCoords, String)]): Unit =
      remaining match
        case Nil => ()
        case ((centerX, centerY), spritePath) :: tail =>
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
    pane

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
    wavePanel.layoutY = 30
    wavePanel.onMouseClicked = event => event.consume()
    wavePanel.mouseTransparent = true
    pauseButton.layoutX = 1050
    pauseButton.layoutY = 30
    overlayPane

  private def renderSingleHealthBar(pane: Pane): ((PhysicalCoords, Double, Color, Double, Double, Double)) => Unit =
    case ((centerX, centerY), percentage, color, barWidth, barHeight, offsetY) =>
      val backgroundBar = new Rectangle:
        this.x = centerX - barWidth / 2
        this.y = centerY + offsetY
        width = barWidth
        height = barHeight
        fill = Color.DarkGray
        stroke = Color.Black
        strokeWidth = 0.5

      val healthBar = new Rectangle:
        this.x = centerX - barWidth / 2
        this.y = centerY + offsetY
        width = barWidth * percentage
        height = barHeight
        fill = color

      pane.children.addAll(backgroundBar, healthBar)

  def cleanup(): Unit =
    gridPane = None
    entityPane = None
    projectilePane = None
    gameStackPane = None
    healthBarPane = None
    wizardButtons = Map.empty
    WavePanel.updateWave()