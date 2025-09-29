package it.unibo.pps.wvt.view
import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.ecs.systems.RenderSystem
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{GridPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.application.Platform
import scalafx.scene.Cursor
import scala.collection.mutable

object ShopPanel:
  private var currentElixirText: Option[Text] = None
  private val wizardButtons: mutable.Map[WizardType, Button] = mutable.Map.empty
  private val wizardCards: mutable.Map[WizardType, VBox] = mutable.Map.empty
  private var lastElixirAmount: Int = -1
  private val buttonStates: mutable.Map[WizardType, Boolean] = mutable.Map.empty
  private lazy val renderSystem = RenderSystem()
  private var isShopOpen: Boolean = true
  private var shopContent: Option[scalafx.scene.layout.VBox] = None
  private var shopPanel: Option[VBox] = None

  def createShopPanel(): VBox =
    wizardButtons.clear()
    wizardCards.clear()
    buttonStates.clear()
    lastElixirAmount = -1
    val elixirDisplay = createElixirDisplay()
    val wizardGrid = createWizardGrid()
    val contentContainer = new scalafx.scene.layout.VBox:
      spacing = 16
      padding = Insets(10, 20, 20, 20)
      alignment = Pos.TopCenter
      children = Seq(elixirDisplay, wizardGrid)
    shopContent = Some(contentContainer)
    val panel = new VBox:
      spacing = 16
      padding = Insets(120, 20, 20, 20)
      alignment = Pos.TopCenter
      prefWidth = 250
      maxWidth = 250
      minWidth = 250
      children = Seq(contentContainer)
    currentElixirText = Some(elixirDisplay)
    shopPanel = Some(panel)
    updatePanelBackground(panel)
    panel

  def createShopButton(): Button =
    val shopButtonConfig = ButtonConfig("Shop", 200, 100, 20, "Times New Roman")
    createStyledButton(shopButtonConfig)(toggleShop())

  private def toggleShop(): Unit =
    isShopOpen = !isShopOpen
    shopContent.foreach: content =>
      content.visible = isShopOpen
      content.managed = isShopOpen
    shopPanel.foreach(updatePanelBackground)

  private def updatePanelBackground(panel: VBox): Unit =
    if isShopOpen then
      panel.style = """-fx-background-image: url('/shop_background.jpg');
                     -fx-background-size: cover;
                     -fx-background-repeat: no-repeat;
                     -fx-background-position: center;
                     -fx-background-radius: 10;
                     -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8,0,2,2);"""
    else panel.style = ""

  private def createWizardGrid(): GridPane =
    val wizardTypes = WizardType.values.toSeq
    val grid = new GridPane:
      hgap = 10
      vgap = 10
      alignment = Pos.Center
    wizardTypes.zipWithIndex.foreach: (wizardType, index) =>
      val row = index / 2
      val col = index % 2
      grid.add(createWizardCard(wizardType), col, row)
    grid

  private def createElixirDisplay(): Text =
    val currentElixir = ViewController.getController.map(_.getCurrentElixir).getOrElse(INITIAL_ELIXIR)
    new Text(s"Elixir: $currentElixir"):
      font = Font.font("Arial", FontWeight.Bold, 13)
      fill = Color.LightBlue

  def updateElixir(): Unit = Platform.runLater:
    val currentElixir = ViewController.getController.map(_.getCurrentElixir).getOrElse(INITIAL_ELIXIR)
    if currentElixir != lastElixirAmount then
      currentElixirText.foreach(_.text = s"Elixir: $currentElixir")
      updateCardStates(currentElixir)
      lastElixirAmount = currentElixir

  private def updateCardStates(currentElixir: Int): Unit =
    wizardCards.foreach: (wizardType, card) =>
      val cost = getWizardCost(wizardType)
      val canAfford = currentElixir >= cost
      val previousState = buttonStates.getOrElse(wizardType, !canAfford)
      if canAfford != previousState || previousState == canAfford then
        buttonStates.update(wizardType, canAfford)
        updateCardStyle(card, canAfford, wizardType)

  private def updateCardStyle(card: VBox, canAfford: Boolean, wizardType: WizardType): Unit =
    if canAfford then
      card.style = """-fx-background-color: rgba(40,40,40,0.9);
                      -fx-background-radius: 6;
                      -fx-padding: 12;
                      -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3,0,1,1);
                      -fx-border-color: #4a90e2;
                      -fx-border-width: 2;
                      -fx-border-radius: 6;"""
      card.cursor = Cursor.Hand
      card.onMouseClicked = event =>
        event.consume()
        Platform.runLater(() => handleWizardPurchase(wizardType))
      card.onMouseEntered = _ =>
        card.style = """-fx-background-color: rgba(60,60,60,0.95);
                        -fx-background-radius: 6;
                        -fx-padding: 12;
                        -fx-effect: dropshadow(gaussian, rgba(74,144,226,0.6), 5,0,1,1);
                        -fx-border-color: #5ba0f2;
                        -fx-border-width: 2;
                        -fx-border-radius: 6;"""
      card.onMouseExited = _ =>
        card.style = """-fx-background-color: rgba(40,40,40,0.9);
                        -fx-background-radius: 6;
                        -fx-padding: 12;
                        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3,0,1,1);
                        -fx-border-color: #4a90e2;
                        -fx-border-width: 2;
                        -fx-border-radius: 6;"""
    else
      card.style = """-fx-background-color: rgba(40,40,40,0.6);
                      -fx-background-radius: 6;
                      -fx-padding: 12;
                      -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3,0,1,1);
                      -fx-border-color: #666666;
                      -fx-border-width: 1;
                      -fx-border-radius: 6;"""
      card.cursor = Cursor.Default
      card.onMouseClicked = null
      card.onMouseEntered = null
      card.onMouseExited = null

  private def getDisplayName(wizardType: WizardType): String = wizardType match
    case WizardType.Generator => "Gen"
    case other => other.toString

  private def createWizardCard(wizardType: WizardType): VBox =
    val cost = getWizardCost(wizardType)
    val imagePath = getWizardImagePath(wizardType)
    val canAfford = ViewController.getController.exists(_.getCurrentElixir >= cost)
    val imageView = createImageView(imagePath, 50) match
      case Right(img) =>
        img.fitWidth = 50
        img.fitHeight = 50
        img.preserveRatio = false
        img
      case Left(_) => new ImageView()
    val nameText = new Text(getDisplayName(wizardType)):
      font = Font.font("Arial", FontWeight.Bold, 12)
      fill = Color.White
      wrappingWidth = 100
      textAlignment = scalafx.scene.text.TextAlignment.Center
    val costText = new Text(s"$cost ♦"):
      font = Font.font("Arial", FontWeight.Bold, 11)
      fill = if canAfford then Color.LightBlue else Color.Gray
    val card = new VBox:
      spacing = 4
      alignment = Pos.Center
      prefWidth = 100
      prefHeight = 110
      minWidth = 100
      minHeight = 110
      maxWidth = 100
      maxHeight = 110
      children = Seq(nameText, imageView, costText)
    wizardCards.update(wizardType, card)
    updateCardStyle(card, canAfford, wizardType)
    buttonStates.update(wizardType, canAfford)
    card

  private def handleWizardPurchase(wizardType: WizardType): Unit =
    handleAction(PlacingWizard(wizardType))

  def getWizardCost(wizardType: WizardType): Int = wizardType match
    case WizardType.Generator => GENERATOR_WIZARD_COST
    case WizardType.Wind => WIND_WIZARD_COST
    case WizardType.Barrier => BARRIER_WIZARD_COST
    case WizardType.Fire => FIRE_WIZARD_COST
    case WizardType.Ice => ICE_WIZARD_COST

  private def getWizardImagePath(wizardType: WizardType): String = wizardType match
    case WizardType.Generator => "/wizard/generator.png"
    case WizardType.Wind => "/wizard/wind.png"
    case WizardType.Barrier => "/wizard/barrier.png"
    case WizardType.Fire => "/wizard/fire.png"
    case WizardType.Ice => "/wizard/ice.png"