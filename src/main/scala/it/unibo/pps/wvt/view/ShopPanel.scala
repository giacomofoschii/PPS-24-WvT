package it.unibo.pps.wvt.view
import it.unibo.pps.wvt.ecs.components.WizardType
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
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
  private val wizardCards: mutable.Map[WizardType, VBox] = mutable.Map.empty
  private val costTexts: mutable.Map[WizardType, Text] = mutable.Map.empty
  private var lastElixirAmount: Int = -1
  private val buttonStates: mutable.Map[WizardType, Boolean] = mutable.Map.empty
  private var isShopOpen: Boolean = true
  private var shopContent: Option[VBox] = None
  private var shopPanel: Option[VBox] = None

  def createShopPanel(): VBox =
    wizardCards.clear()
    costTexts.clear()
    buttonStates.clear()
    lastElixirAmount = -1
    val elixirDisplay = createElixirDisplay()
    val wizardGrid = createWizardGrid()
    val contentContainer = new VBox:
      spacing = SHOP_PANEL_SPACING
      padding = Insets(SHOP_CONTENT_TOP_PADDING, SHOP_PANEL_SIDE_PADDING, SHOP_PANEL_BOTTOM_PADDING, SHOP_PANEL_SIDE_PADDING)
      alignment = Pos.TopCenter
      children = Seq(elixirDisplay, wizardGrid)
    shopContent = Some(contentContainer)
    val panel = new VBox:
      spacing = SHOP_PANEL_SPACING
      padding = Insets(SHOP_PANEL_TOP_PADDING, SHOP_PANEL_SIDE_PADDING, SHOP_PANEL_BOTTOM_PADDING, SHOP_PANEL_SIDE_PADDING)
      alignment = Pos.TopCenter
      prefWidth = SHOP_PANEL_WIDTH
      maxWidth = SHOP_PANEL_WIDTH
      minWidth = SHOP_PANEL_WIDTH
      children = Seq(contentContainer)
    currentElixirText = Some(elixirDisplay)
    shopPanel = Some(panel)
    val clipRect = new scalafx.scene.shape.Rectangle:
      width <== panel.width
      height <== panel.height
      arcWidth = SHOP_PANEL_BORDER_RADIUS * 2
      arcHeight = SHOP_PANEL_BORDER_RADIUS * 2
    panel.clip = clipRect
    updatePanelBackground(panel)
    panel

  def createShopButton(): Button =
    val shopButtonConfig = ButtonConfig("Shop", SHOP_BUTTON_WIDTH, SHOP_BUTTON_HEIGHT, SHOP_BUTTON_FONT_SIZE, "Times New Roman")
    createStyledButton(shopButtonConfig)(toggleShop())

  private def toggleShop(): Unit =
    isShopOpen = !isShopOpen
    shopContent.foreach: content =>
      content.visible = isShopOpen
      content.managed = isShopOpen
    shopPanel.foreach(updatePanelBackground)

  private def updatePanelBackground(panel: VBox): Unit =
    if isShopOpen then
      panel.style = s"""-fx-background-image: url('/shop_background.jpg');
                       -fx-background-size: cover;
                       -fx-background-repeat: no-repeat;
                       -fx-background-position: center;
                       -fx-background-radius: $SHOP_PANEL_BORDER_RADIUS;
                       -fx-border-radius: $SHOP_PANEL_BORDER_RADIUS;
                       -fx-border-color: transparent;
                       -fx-border-width: 1;
                       -fx-effect: dropshadow(gaussian, rgba(0,0,0,$SHOP_SHADOW_OPACITY), $SHOP_SHADOW_RADIUS,0,$SHOP_SHADOW_OFFSET_X,$SHOP_SHADOW_OFFSET_Y);"""
    else panel.style = ""

  private def createWizardGrid(): GridPane =
    val wizardTypes = WizardType.values.toSeq
    val grid = new GridPane:
      hgap = SHOP_GRID_GAP
      vgap = SHOP_GRID_GAP
      alignment = Pos.Center
    wizardTypes.zipWithIndex.foreach: (wizardType, index) =>
      val row = index / SHOP_CARD_COLUMNS
      val col = index % SHOP_CARD_COLUMNS
      grid.add(createWizardCard(wizardType), col, row)
    grid

  private def createElixirDisplay(): Text =
    val currentElixir = ViewController.getController.map(_.getCurrentElixir).getOrElse(INITIAL_ELIXIR)
    new Text(s"Elixir: $currentElixir"):
      font = Font.font("Arial", FontWeight.Bold, ELIXIR_FONT_SIZE)
      fill = Color.web(SHOP_PRIMARY_COLOR)

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
    costTexts.get(wizardType).foreach(_.fill = Color.web(SHOP_PRIMARY_COLOR))

    // PRIMA rimuovi TUTTI gli handler esistenti
    card.onMouseClicked = null
    card.onMouseEntered = null
    card.onMouseExited = null

    if canAfford then
      card.style = s"""-fx-background-color: rgba($SHOP_CARD_BG_R,$SHOP_CARD_BG_G,$SHOP_CARD_BG_B,$SHOP_CARD_BG_OPACITY);
                      -fx-background-radius: $SHOP_CARD_BORDER_RADIUS;
                      -fx-padding: $SHOP_CARD_PADDING;
                      -fx-effect: dropshadow(gaussian, rgba(0,0,0,$SHOP_CARD_SHADOW_OPACITY), $SHOP_CARD_SHADOW_RADIUS,0,$SHOP_CARD_SHADOW_OFFSET_X,$SHOP_CARD_SHADOW_OFFSET_Y);
                      -fx-border-color: $SHOP_PRIMARY_COLOR;
                      -fx-border-width: $SHOP_CARD_BORDER_WIDTH_ACTIVE;
                      -fx-border-radius: $SHOP_CARD_BORDER_RADIUS;"""
      card.cursor = Cursor.Hand
      card.onMouseClicked = event =>
        event.consume()
        println(s"[SHOP] Card cliccata: $wizardType")  // <-- DEBUG
        handleWizardPurchase(wizardType)

      card.onMouseEntered = _ =>
        card.style = s"""-fx-background-color: rgba($SHOP_CARD_HOVER_BG_R,$SHOP_CARD_HOVER_BG_G,$SHOP_CARD_HOVER_BG_B,$SHOP_CARD_HOVER_BG_OPACITY);
                        -fx-background-radius: $SHOP_CARD_BORDER_RADIUS;
                        -fx-padding: $SHOP_CARD_PADDING;
                        -fx-effect: dropshadow(gaussian, rgba($SHOP_CARD_HOVER_SHADOW_R,$SHOP_CARD_HOVER_SHADOW_G,$SHOP_CARD_HOVER_SHADOW_B,$SHOP_CARD_HOVER_SHADOW_OPACITY), $SHOP_CARD_HOVER_SHADOW_RADIUS,0,$SHOP_CARD_SHADOW_OFFSET_X,$SHOP_CARD_SHADOW_OFFSET_Y);
                        -fx-border-color: $SHOP_HOVER_COLOR;
                        -fx-border-width: $SHOP_CARD_BORDER_WIDTH_ACTIVE;
                        -fx-border-radius: $SHOP_CARD_BORDER_RADIUS;"""

      card.onMouseExited = _ =>
        card.style = s"""-fx-background-color: rgba($SHOP_CARD_BG_R,$SHOP_CARD_BG_G,$SHOP_CARD_BG_B,$SHOP_CARD_BG_OPACITY);
                        -fx-background-radius: $SHOP_CARD_BORDER_RADIUS;
                        -fx-padding: $SHOP_CARD_PADDING;
                        -fx-effect: dropshadow(gaussian, rgba(0,0,0,$SHOP_CARD_SHADOW_OPACITY), $SHOP_CARD_SHADOW_RADIUS,0,$SHOP_CARD_SHADOW_OFFSET_X,$SHOP_CARD_SHADOW_OFFSET_Y);
                        -fx-border-color: $SHOP_PRIMARY_COLOR;
                        -fx-border-width: $SHOP_CARD_BORDER_WIDTH_ACTIVE;
                        -fx-border-radius: $SHOP_CARD_BORDER_RADIUS;"""
    else
      card.style = s"""-fx-background-color: rgba($SHOP_CARD_BG_R,$SHOP_CARD_BG_G,$SHOP_CARD_BG_B,$SHOP_CARD_DISABLED_OPACITY);
                      -fx-background-radius: $SHOP_CARD_BORDER_RADIUS;
                      -fx-padding: $SHOP_CARD_PADDING;
                      -fx-effect: dropshadow(gaussian, rgba(0,0,0,$SHOP_CARD_SHADOW_OPACITY), $SHOP_CARD_SHADOW_RADIUS,0,$SHOP_CARD_SHADOW_OFFSET_X,$SHOP_CARD_SHADOW_OFFSET_Y);
                      -fx-border-color: $SHOP_DISABLED_COLOR;
                      -fx-border-width: $SHOP_CARD_BORDER_WIDTH_DISABLED;
                      -fx-border-radius: $SHOP_CARD_BORDER_RADIUS;"""
      card.cursor = Cursor.Default
      card.onMouseClicked = event => event.consume()

  private def getDisplayName(wizardType: WizardType): String = wizardType match
    case WizardType.Generator => "Generator"
    case other => other.toString

  private def createWizardCard(wizardType: WizardType): VBox =
    val cost = getWizardCost(wizardType)
    val imagePath = getWizardImagePath(wizardType)
    val canAfford = ViewController.getController.exists(_.getCurrentElixir >= cost)
    val imageView = createImageView(imagePath, SHOP_CARD_IMAGE_SIZE) match
      case Right(img) =>
        img.fitWidth = SHOP_CARD_IMAGE_SIZE
        img.fitHeight = SHOP_CARD_IMAGE_SIZE
        img.preserveRatio = false
        img
      case Left(_) => new ImageView()
    val nameText = new Text(getDisplayName(wizardType)):
      font = Font.font("Arial", FontWeight.Bold, WIZARD_NAME_FONT_SIZE)
      fill = Color.web(SHOP_PRIMARY_COLOR)
      wrappingWidth = WIZARD_NAME_WIDTH
      textAlignment = scalafx.scene.text.TextAlignment.Center
    val costText = new Text(s"$cost ♦"):
      font = Font.font("Arial", FontWeight.Bold, WIZARD_COST_FONT_SIZE)
      fill = Color.web(SHOP_PRIMARY_COLOR)
    costTexts.update(wizardType, costText)
    val card = new VBox:
      spacing = SHOP_CARD_SPACING
      alignment = Pos.Center
      prefWidth = SHOP_CARD_WIDTH
      prefHeight = SHOP_CARD_HEIGHT
      minWidth = SHOP_CARD_WIDTH
      minHeight = SHOP_CARD_HEIGHT
      maxWidth = SHOP_CARD_WIDTH
      maxHeight = SHOP_CARD_HEIGHT
      children = Seq(nameText, imageView, costText)
      mouseTransparent = false
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