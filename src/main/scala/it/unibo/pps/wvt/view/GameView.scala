package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.utilities.GridMapper.PhysicalCoords
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.geometry.Insets
import scalafx.scene.Parent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, Pane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.application.Platform


object GameView:
  private var gridPane: Option[Pane] = None
  private var entityPane: Option[Pane] = None
  private var wizardButtons: Map[WizardType, Button] = Map.empty
  private var gameStackPane: Option[StackPane] = None
  private var healthBarPane: Option[Pane] = None

  def apply(): Parent =
    gridPane = None
    entityPane = None
    gameStackPane = None
    wizardButtons = Map.empty

    lazy val backgroundImage =
      createBackgroundView("/background_grid.png", GAME_MAP_SCALE_FACTOR).getOrElse(new ImageView())

    val gridOverlay = new Pane()
    gridOverlay.mouseTransparent = true

    val entityOverlay = new Pane()

    val uiOverlay = createUIOverlay

    val healthBarOverlay = new Pane()
    healthBarOverlay.mouseTransparent = true

    val stackPane = new StackPane:
      children = Seq(backgroundImage, gridOverlay, entityOverlay, uiOverlay)

      onMouseClicked = event =>
        handleGridClick(event.getX, event.getY)

    healthBarPane = Some(healthBarOverlay)
    entityPane = Some(entityOverlay)
    gridPane = Some(gridOverlay)
    gameStackPane = Some(stackPane)

    stackPane

  def drawGrid(greenCells: Seq[PhysicalCoords], redCells: Seq[PhysicalCoords]): Unit =
    // Ensure UI updates happen on JavaFX Application Thread
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
      entityPane.foreach(pane =>
        pane.children.clear()
        entities.foreach { case ((x, y), spritePath) =>
          createImageView(spritePath, CELL_WIDTH) match
            case Right(imageView) =>
              // Center the image in the cell
              imageView.x = x
              imageView.y = y
              imageView.fitWidth = CELL_WIDTH
              imageView.fitHeight = CELL_HEIGHT
              imageView.preserveRatio = false  // Force exact cell dimensions
              pane.children.add(imageView)
            case Left(error) =>
              println(s"Error loading entity image: $error")
        }
      )

  def renderHealthBars(healthBars: Seq[(PhysicalCoords, Double, Color, Double, Double, Double)]): Unit =
    Platform.runLater:
      healthBarPane.foreach: pane =>
        pane.children.clear()
        healthBars.foreach(renderSingleHealthBar(pane))
  
  def showError(message: String) : Unit =
    Platform.runLater:
      new Alert(AlertType.Error):
        title = "Error"
        headerText = "An error occurred"
        contentText = message
      .showAndWait()

  private def handleGridClick(x: Double, y: Double): Unit =
    println(s"[GameView] Click at ($x, $y)")
    ViewController.getController match
      case Some(controller) =>
        // Check if the game is in pause after to process the click
        if !controller.getEngine.isPaused then
          controller.handleMouseClick(x.toInt, y.toInt)
        else
          println("[INPUT] Click ignored - game is paused")
      case None =>
        println("[ERROR] No controller available for input handling")

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

    new BorderPane:
      top = new BorderPane:
        padding = Insets(5)
        right = pauseButton
      left = shopPanel

  private def renderSingleHealthBar(pane: Pane): ((PhysicalCoords, Double, Color, Double, Double, Double)) => Unit =
    case ((myX, myY), percentage, color, barWidth, barHeight, offsetY) =>
      // Background della health bar
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
    gameStackPane = None
    wizardButtons = Map.empty
