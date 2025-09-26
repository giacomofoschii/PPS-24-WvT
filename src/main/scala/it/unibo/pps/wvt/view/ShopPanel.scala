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

import scala.collection.mutable

object ShopPanel:

  private var currentElixirText: Option[Text] = None
  private val wizardButtons: mutable.Map[WizardType, Button] = mutable.Map.empty
  private var lastElixirAmount: Int = -1
  private val buttonStates: mutable.Map[WizardType, Boolean] = mutable.Map.empty
  private lazy val renderSystem = new RenderSystem()
  private var isShopOpen: Boolean = true
  private var shopContent: Option[scalafx.scene.layout.VBox] = None

  def createShopPanel(): VBox =
    val elixirDisplay = createElixirDisplay()
    val wizardGrid = createWizardGrid()

    // Create content container with the black background
    val contentContainer = new scalafx.scene.layout.VBox:
      spacing = 16
      padding = Insets(20)
      alignment = Pos.TopCenter
      style = """-fx-background-color: rgba(0,0,0,0.85);
                 -fx-background-radius: 10;
                 -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8,0,2,2);"""
      children = Seq(elixirDisplay, wizardGrid)

    shopContent = Some(contentContainer)

    // Main panel with just content (no shop button)
    val panel = new VBox:
      spacing = 16
      padding = Insets(120, 20, 20, 20)  // Extra top padding to leave space for shop button
      alignment = Pos.TopCenter
      prefWidth = 250
      maxWidth = 250
      children = Seq(contentContainer)

    currentElixirText = Some(elixirDisplay)
    panel

  def createShopButton(): Button =
    val shopButtonConfig = ButtonConfig("Shop", 200, 100, 20, "Times New Roman")
    createStyledButton(shopButtonConfig)(toggleShop())

  private def toggleShop(): Unit =
    isShopOpen = !isShopOpen
    shopContent.foreach: content =>
      content.visible = isShopOpen
      content.managed = isShopOpen

  private def createWizardGrid(): GridPane =
    val wizardTypes = WizardType.values.toSeq
    val grid = new GridPane:
      hgap = 14
      vgap = 14
      alignment = Pos.Center

    wizardTypes.zipWithIndex.foreach: (wizardType, index) =>
      val row = index / 2
      val col = index % 2
      grid.add(createWizardCard(wizardType), col, row)

    grid

  def createElixirDisplay(): Text =
    val currentElixir = ViewController.getController.map(_.getCurrentElixir).getOrElse(0)
    new Text(s"Elixir: $currentElixir"):
      font = Font.font("Arial", FontWeight.Bold, 13)
      fill = Color.LightBlue

  def updateElixir(): Unit =
    val currentElixir = ViewController.getController.map(_.getCurrentElixir).getOrElse(0)

    if currentElixir != lastElixirAmount then
      currentElixirText.foreach(_.text = s"Elixir: $currentElixir")
      updateButtonStates(currentElixir)
      lastElixirAmount = currentElixir

  private def updateButtonStates(currentElixir: Int): Unit =
    wizardButtons.foreach: (wizardType, button) =>
      val cost = getWizardCost(wizardType)
      val canAfford = currentElixir >= cost
      val previousState = buttonStates.getOrElse(wizardType, !canAfford)

      if canAfford != previousState then
        button.disable = !canAfford
        buttonStates.update(wizardType, canAfford)

        if canAfford then
          button.style = """-fx-background-color: linear-gradient(#4a90e2, #357abd);
                           -fx-text-fill: white;
                           -fx-background-radius: 10;
                           -fx-border-radius: 10;
                           -fx-cursor: hand;"""
          button.onAction = _ => handleWizardPurchase(wizardType)
          button.onMouseEntered = _ =>
            button.style = """-fx-background-color: linear-gradient(#5ba0f2, #4589cd);
                             -fx-text-fill: white;
                             -fx-background-radius: 10;
                             -fx-border-radius: 10;
                             -fx-cursor: hand;"""
          button.onMouseExited = _ =>
            button.style = """-fx-background-color: linear-gradient(#4a90e2, #357abd);
                             -fx-text-fill: white;
                             -fx-background-radius: 10;
                             -fx-border-radius: 10;
                             -fx-cursor: hand;"""
        else
          button.style = """-fx-background-color: #666666;
                           -fx-text-fill: #cccccc;
                           -fx-background-radius: 10;
                           -fx-border-radius: 10;"""
          button.onAction = null
          button.onMouseEntered = null
          button.onMouseExited = null

  private def createWizardCard(wizardType: WizardType): VBox =
    val cost = getWizardCost(wizardType)
    val imagePath = renderSystem.getWizardImagePath(wizardType)
    val canAfford = ViewController.getController.exists(_.getCurrentElixir >= cost)

    val imageView = createImageView(imagePath, 50) match
      case Right(img) =>
        img.fitWidth = 50
        img.fitHeight = 50
        img.preserveRatio = false
        img
      case Left(_) => new ImageView()

    val nameText = new Text(wizardType.toString):
      font = Font.font("Arial", FontWeight.Bold, 12)
      fill = Color.White

    val buyButton = createWizardBuyButton(wizardType, cost, canAfford)
    wizardButtons.update(wizardType, buyButton)

    new VBox:
      spacing = 4
      alignment = Pos.Center
      prefWidth = 120
      prefHeight = 120
      style = """-fx-background-color: rgba(40,40,40,0.9);
                 -fx-background-radius: 6;
                 -fx-padding: 12;
                 -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3,0,1,1);"""
      children = Seq(nameText, imageView, buyButton)

  private def createWizardBuyButton(wizardType: WizardType, cost: Int, canAfford: Boolean): Button =
    val buttonText = s"$cost ♦"
    val buttonConfig = ButtonConfig(buttonText, 130, 40, 11, "Arial")

    val button = new Button(buttonText):
      font = Font.font(buttonConfig.fontFamily, FontWeight.Bold, buttonConfig.fontSize)
      prefWidth = buttonConfig.width
      prefHeight = buttonConfig.height
      disable = !canAfford

      if canAfford then
        style = """-fx-background-color: linear-gradient(#4a90e2, #357abd);
                   -fx-text-fill: white;
                   -fx-background-radius: 10;
                   -fx-border-radius: 10;
                   -fx-cursor: hand;"""
        onAction = _ => handleWizardPurchase(wizardType)
        onMouseEntered = _ =>
          style = """-fx-background-color: linear-gradient(#5ba0f2, #4589cd);
                     -fx-text-fill: white;
                     -fx-background-radius: 10;
                     -fx-border-radius: 10;
                     -fx-cursor: hand;"""
        onMouseExited = _ =>
          style = """-fx-background-color: linear-gradient(#4a90e2, #357abd);
                     -fx-text-fill: white;
                     -fx-background-radius: 10;
                     -fx-border-radius: 10;
                     -fx-cursor: hand;"""
      else
        style = """-fx-background-color: #666666;
                   -fx-text-fill: #cccccc;
                   -fx-background-radius: 10;
                   -fx-border-radius: 10;"""

    button

  private def handleWizardPurchase(wizardType: WizardType): Unit =
    ViewController.getController.foreach: controller =>
      controller.selectWizard(wizardType)

  private def getWizardCost(wizardType: WizardType): Int = wizardType match
    case WizardType.Generator => GENERATOR_WIZARD_COST
    case WizardType.Wind => WIND_WIZARD_COST
    case WizardType.Barrier => BARRIER_WIZARD_COST
    case WizardType.Fire => FIRE_WIZARD_COST
    case WizardType.Ice => ICE_WIZARD_COST