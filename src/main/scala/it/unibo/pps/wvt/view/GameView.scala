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
        handleGridClick(event.getX, event.getY)

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

        greenCells.foreach: (x, y) =>
          pane.children.add(createStatusCell(x, y, Color.Green))

        redCells.foreach: (x, y) =>
          pane.children.add(createStatusCell(x, y, Color.Red))

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
        healthBars.foreach(renderSingleHealthBar(pane))

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
    entities.foreach { case ((x, y), spritePath) =>
      val isProjectile = spritePath.contains("/projectile/")
      createImageView(spritePath, CELL_WIDTH) match
        case Right(imageView) =>
          imageView.preserveRatio = true
          // Centra l'immagine nella cella
          imageView.x = x + (CELL_WIDTH - 75) / 2
          imageView.y = y + (CELL_HEIGHT - 90) / 2
          pane.children.add(imageView)
        case Left(error) =>
          println(s"Error loading image: $error")
    }
    pane

  private def createUIOverlay: Pane =
    val buttonConfigs = Map(
      "pause" -> ButtonConfig("Pause", 200, 100, 20, "Times New Roman"),
    )
    val pauseButton = createStyledButton(buttonConfigs("pause"))(handleAction(PauseGame))
    val shopPanel = ShopPanel.createShopPanel()
    val shopButton = ShopPanel.createShopButton()

    val overlayPane = new Pane {
      children = Seq(shopPanel, pauseButton, shopButton)
    }

    shopPanel.layoutX = 10
    shopPanel.layoutY = 10
    shopPanel.prefHeight <== overlayPane.height - 20
    shopPanel.maxHeight <== overlayPane.height - 20
    shopButton.layoutX = shopPanel.layoutX.value + (250 - 200) / 2
    shopButton.layoutY = 30
    pauseButton.layoutX = 1050
    pauseButton.layoutY = 30

    overlayPane

  private def renderSingleHealthBar(pane: Pane): ((PhysicalCoords, Double, Color, Double, Double, Double)) => Unit =
    case ((myX, myY), percentage, color, barWidth, barHeight, offsetY) =>
      val backgroundBar = new Rectangle:
        this.x = myX + (CELL_WIDTH - barWidth) / 2
        this.y = myY + offsetY
        width = barWidth
        height = barHeight
        fill = Color.DarkGray
        stroke = Color.Black
        strokeWidth = 0.5

      val healthBar = new Rectangle:
        this.x = myX + (CELL_WIDTH - barWidth) / 2
        this.y = myY + offsetY
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