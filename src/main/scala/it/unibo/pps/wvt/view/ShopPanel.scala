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
    // Reset state when creating new panel
    wizardButtons.clear()
    wizardCards.clear()
    buttonStates.clear()
    lastElixirAmount = -1

    val elixirDisplay = createElixirDisplay()
    val wizardGrid = createWizardGrid()

    // Create content container (just the shop content, no background)
    val contentContainer = new scalafx.scene.layout.VBox:
      spacing = 16
      padding = Insets(10, 20, 20, 20)
      alignment = Pos.TopCenter
      children = Seq(elixirDisplay, wizardGrid)

    shopContent = Some(contentContainer)

    // Main panel that will show black background only when shop is open
    val panel = new VBox:
      spacing = 16
      padding = Insets(120, 20, 20, 20)  // Keep space for the fixed shop button above
      alignment = Pos.TopCenter
      prefWidth = 250
      maxWidth = 250
      minWidth = 250  // Assicura che la larghezza sia esattamente 250
      children = Seq(contentContainer)

    currentElixirText = Some(elixirDisplay)

    // Save panel reference and set initial background
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

    // Update panel background based on shop state
    shopPanel.foreach(updatePanelBackground)

  private def updatePanelBackground(panel: VBox): Unit =
    if isShopOpen then
      panel.style = """-fx-background-color: rgba(0,0,0,0.85);
                       -fx-background-radius: 10;
                       -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8,0,2,2);"""
    else
      panel.style = "" // No background when closed

  private def createWizardGrid(): GridPane =
    val wizardTypes = WizardType.values.toSeq
    val grid = new GridPane:
      hgap = 10  // Ridotto leggermente per compensare le card più piccole
      vgap = 10  // Ridotto leggermente per compensare le card più piccole
      alignment = Pos.Center

    wizardTypes.zipWithIndex.foreach: (wizardType, index) =>
      val row = index / 2
      val col = index % 2
      grid.add(createWizardCard(wizardType), col, row)

    grid

  def createElixirDisplay(): Text =
    val currentElixir = ViewController.getController.map(_.getCurrentElixir).getOrElse(INITIAL_ELIXIR)
    new Text(s"Elixir: $currentElixir"):
      font = Font.font("Arial", FontWeight.Bold, 13)
      fill = Color.LightBlue

  def updateElixir(): Unit =
    Platform.runLater {
      val currentElixir = ViewController.getController.map(_.getCurrentElixir).getOrElse(INITIAL_ELIXIR)

      if currentElixir != lastElixirAmount then
        currentElixirText.foreach(_.text = s"Elixir: $currentElixir")
        updateCardStates(currentElixir)
        lastElixirAmount = currentElixir
    }

  private def updateCardStates(currentElixir: Int): Unit =
    wizardCards.foreach: (wizardType, card) =>
      val cost = getWizardCost(wizardType)
      val canAfford = currentElixir >= cost
      val previousState = buttonStates.getOrElse(wizardType, !canAfford)

      if canAfford != previousState then
        buttonStates.update(wizardType, canAfford)
        updateCardStyle(card, canAfford, wizardType)
      else if previousState == canAfford then
        // Assicurati che lo stile sia applicato anche se lo stato non è cambiato
        updateCardStyle(card, canAfford, wizardType)

  private def updateCardStyle(card: VBox, canAfford: Boolean, wizardType: WizardType): Unit =
    println(s"[ShopPanel] Updating card style for $wizardType, canAfford: $canAfford")

    if canAfford then
      card.style = """-fx-background-color: rgba(40,40,40,0.9);
                      -fx-background-radius: 6;
                      -fx-padding: 12;
                      -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3,0,1,1);
                      -fx-border-color: #4a90e2;
                      -fx-border-width: 2;
                      -fx-border-radius: 6;"""
      card.cursor = Cursor.Hand
      card.onMouseClicked = event => {
        println(s"[ShopPanel] Card clicked for $wizardType")
        event.consume() // Consuma l'evento per evitare propagazione
        Platform.runLater(() => handleWizardPurchase(wizardType))
      }
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
    case WizardType.Generator => "Gen"  // Abbrevia Generator
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
      wrappingWidth = 100 // Limita la larghezza del testo
      textAlignment = scalafx.scene.text.TextAlignment.Center

    val costText = new Text(s"$cost ♦"):
      font = Font.font("Arial", FontWeight.Bold, 11)
      fill = if canAfford then Color.LightBlue else Color.Gray

    val card = new VBox:
      spacing = 4
      alignment = Pos.Center
      prefWidth = 100  // Dimensione fissa
      prefHeight = 110 // Dimensione fissa
      minWidth = 100   // Dimensione minima
      minHeight = 110  // Dimensione minima
      maxWidth = 100   // Dimensione massima
      maxHeight = 110  // Dimensione massima
      children = Seq(nameText, imageView, costText)

    // Store card reference for later updates
    wizardCards.update(wizardType, card)

    // Set initial state and ensure click handlers are properly set
    updateCardStyle(card, canAfford, wizardType)
    buttonStates.update(wizardType, canAfford)

    card

  private def handleWizardPurchase(wizardType: WizardType): Unit =
    ViewController.getController.foreach: controller =>
      controller.selectWizard(wizardType)
      println(s"[ShopPanel] Wizard $wizardType selected. Now click on the game grid to place it.")

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