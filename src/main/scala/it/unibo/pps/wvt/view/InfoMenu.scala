package it.unibo.pps.wvt.view

import scalafx.scene._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.scene.image._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.scene.paint.Color
import it.unibo.pps.wvt.utilities.ViewConstants._
import it.unibo.pps.wvt.view.ButtonFactory._
import it.unibo.pps.wvt.view.ImageFactory._
import it.unibo.pps.wvt.utilities.GamePlayConstants._

object InfoMenu:

  def apply(): Parent =
    lazy val backgroundImage = createBackgroundView("/main_menu.jpg", MENU_SCALE_FACTOR).getOrElse(new ImageView())
    val contentArea = new StackPane()
    contentArea.prefHeight = INFO_CONTENT_AREA_HEIGHT
    contentArea.minHeight = INFO_CONTENT_AREA_HEIGHT
    contentArea.maxHeight = INFO_CONTENT_AREA_HEIGHT
    lazy val rulesView = createRulesView()
    lazy val wizardsView = createWizardsView()
    lazy val trollsView = createTrollsView()
    contentArea.children = Seq(rulesView)
    val (rulesButton, wizardsButton, trollsButton) = createNavigationButtons(contentArea, rulesView, wizardsView, trollsView)
    val topBar = createTopBar(rulesButton, wizardsButton, trollsButton)
    val bottomBar = createBottomBar()
    val layout = new BorderPane:
      top = topBar
      center = new VBox:
        alignment = Pos.TopCenter
        padding = Insets(INFO_CENTER_PADDING_VERTICAL, INFO_CENTER_PADDING_HORIZONTAL, INFO_CENTER_PADDING_VERTICAL, INFO_CENTER_PADDING_HORIZONTAL)
        children = Seq(contentArea)
      bottom = bottomBar
    new StackPane:
      children = Seq(backgroundImage, layout)

  private def createNavigationButtons(contentArea: StackPane, rulesView: VBox, wizardsView: GridPane, trollsView: GridPane) =
    val buttonConfigs = Map(
      "rules" -> ButtonConfig("Rules", INFO_NAV_BUTTON_WIDTH, INFO_NAV_BUTTON_HEIGHT, INFO_NAV_BUTTON_FONT_SIZE, "Times New Roman"),
      "wizards" -> ButtonConfig("Wizards", INFO_NAV_BUTTON_WIDTH, INFO_NAV_BUTTON_HEIGHT, INFO_NAV_BUTTON_FONT_SIZE, "Times New Roman"),
      "trolls" -> ButtonConfig("Trolls", INFO_NAV_BUTTON_WIDTH, INFO_NAV_BUTTON_HEIGHT, INFO_NAV_BUTTON_FONT_SIZE, "Times New Roman")
    )
    val rulesButton = createStyledButton(buttonConfigs("rules")) {}
    val wizardsButton = createStyledButton(buttonConfigs("wizards")) {}
    val trollsButton = createStyledButton(buttonConfigs("trolls")) {}

    def updateButtonStyles(activeButton: String): Unit =
      rulesButton.opacity = if activeButton == "rules" then INFO_BUTTON_ACTIVE_OPACITY else INFO_BUTTON_INACTIVE_OPACITY
      wizardsButton.opacity = if activeButton == "wizards" then INFO_BUTTON_ACTIVE_OPACITY else INFO_BUTTON_INACTIVE_OPACITY
      trollsButton.opacity = if activeButton == "trolls" then INFO_BUTTON_ACTIVE_OPACITY else INFO_BUTTON_INACTIVE_OPACITY

    rulesButton.onAction = _ =>
      contentArea.children = Seq(rulesView)
      updateButtonStyles("rules")

    wizardsButton.onAction = _ =>
      contentArea.children = Seq(wizardsView)
      updateButtonStyles("wizards")

    trollsButton.onAction = _ =>
      contentArea.children = Seq(trollsView)
      updateButtonStyles("trolls")

    updateButtonStyles("rules")
    (rulesButton, wizardsButton, trollsButton)

  private def createTopBar(rulesButton: Button, wizardsButton: Button, trollsButton: Button): HBox =
    new HBox:
      spacing = INFO_TOP_BAR_SPACING
      alignment = Pos.Center
      padding = Insets(INFO_TOP_BAR_PADDING)
      children = Seq(rulesButton, wizardsButton, trollsButton)

  private def createBottomBar(): BorderPane =
    new BorderPane:
      padding = Insets(PADDING_MENU)
      left = createStyledButton(ButtonConfig("Main Menu", INFO_BOTTOM_BUTTON_WIDTH, INFO_BOTTOM_BUTTON_HEIGHT, INFO_BOTTOM_BUTTON_FONT_SIZE, "Times New Roman"))(handleAction(BackToMenu))
      right = createStyledButton(ButtonConfig("Exit", INFO_BOTTOM_BUTTON_WIDTH, INFO_BOTTOM_BUTTON_HEIGHT, INFO_BOTTOM_BUTTON_FONT_SIZE, "Times New Roman"))(handleAction(ExitGame))

  private def createGoldTitle(text: String, fontSize: Int = INFO_GOLD_TITLE_FONT_SIZE): Text =
    new Text(text):
      font = Font.font("Times New Roman", FontWeight.Bold, fontSize)
      fill = Color.Gold

  private def createStatText(symbol: String, value: String): VBox =
    new VBox:
      spacing = INFO_STAT_TEXT_SPACING
      alignment = Pos.Center
      children = Seq(
        new Text(symbol):
          font = Font.font("Arial", FontWeight.Bold, INFO_STAT_SYMBOL_FONT_SIZE)
          fill = Color.White
        ,
        new Text(value):
          font = Font.font("Arial", INFO_STAT_VALUE_FONT_SIZE)
          fill = Color.LightGray
      )

  private def legendItem(symbol: String, textLabel: String): VBox =
    new VBox:
      spacing = INFO_LEGEND_SPACING
      alignment = Pos.Center
      children = Seq(
        new Text(symbol):
          font = Font.font("Arial", FontWeight.Bold, INFO_LEGEND_SYMBOL_FONT_SIZE)
          fill = Color.White
        ,
        new Text(textLabel):
          font = Font.font("Arial", INFO_LEGEND_TEXT_FONT_SIZE)
          fill = Color.LightGray
      )

  private def createGrid(cards: Seq[(String, String, String, String, Seq[(String, String)])], cols: Int = INFO_CARD_COLUMNS): GridPane =
    val grid = new GridPane:
      hgap = INFO_CARD_GRID_GAP
      vgap = INFO_CARD_GRID_GAP
      alignment = Pos.Center
    for (card, idx) <- cards.zipWithIndex do
      val (name, stat, ability, imagePath, icons) = card
      grid.add(createCard(name, stat, ability, imagePath, icons), idx % cols, idx / cols)
    grid

  private def createCard(name: String, stat: String, ability: String, imagePath: String, icons: Seq[(String, String)]): VBox =
    val imageView = createImageView(imagePath, INFO_CARD_IMAGE_SIZE) match
      case Right(img) =>
        img.fitWidth = INFO_CARD_IMAGE_SIZE
        img.fitHeight = INFO_CARD_IMAGE_SIZE
        img.preserveRatio = true
        img
      case Left(_) => new ImageView()

    val statBox = new HBox:
      spacing = INFO_CARD_STAT_BOX_SPACING
      alignment = Pos.Center
      children = icons.map((symbol, value) => createStatText(symbol, value))

    val abilityBox = new Text(ability):
      font = Font.font("Arial", FontWeight.Normal, INFO_CARD_ABILITY_FONT_SIZE)
      fill = Color.LightBlue
      wrappingWidth = INFO_CARD_ABILITY_WIDTH
      textAlignment = TextAlignment.Center

    new VBox:
      spacing = INFO_CARD_SPACING
      alignment = Pos.TopCenter
      prefWidth = INFO_CARD_WIDTH
      prefHeight = INFO_CARD_HEIGHT
      style = createCardStyle()
      children = Seq(
        createGoldTitle(name),
        imageView,
        statBox,
        abilityBox
      )

  private def createCardStyle(): String =
    s"""-fx-background-color: rgba(0,0,0,$INFO_CARD_BG_OPACITY);
       -fx-background-radius: $INFO_CARD_BORDER_RADIUS;
       -fx-padding: $INFO_CARD_PADDING;
       -fx-effect: dropshadow(gaussian, rgba(0,0,0,$INFO_CARD_SHADOW_OPACITY), $INFO_CARD_SHADOW_RADIUS,0,0,$INFO_CARD_SHADOW_OFFSET);"""

  private def createRulesBoxStyle(): String =
    s"""-fx-background-color: rgba(0,0,0,$INFO_RULES_BG_OPACITY);
       -fx-background-radius: $INFO_RULES_BORDER_RADIUS;
       -fx-padding: $INFO_RULES_PADDING;
       -fx-effect: dropshadow(gaussian, rgba(0,0,0,$INFO_RULES_SHADOW_OPACITY), $INFO_RULES_SHADOW_RADIUS,0,0,$INFO_RULES_SHADOW_OFFSET);"""

  private def createRulesView(): VBox =
    val rulesContent = new VBox:
      alignment = Pos.TopCenter
      spacing = INFO_RULES_CONTENT_SPACING
      children = Seq(
        new Text(s"\u2666 You start with $INITIAL_ELIXIR elixir to buy wizards"):
          style = baseRuleStyle
        ,
        new Text("🎯 Click empty cells to place wizards and defend against trolls"):
          style = baseRuleStyle
        ,
        new Text("🏰 Generators create elixir - place them first!"):
          style = baseRuleStyle
        ,
        new Text("⚔ Wizards attack trolls automatically when in range"):
          style = baseRuleStyle
        ,
        new HBox:
          spacing = INFO_RULES_LEGEND_SPACING
          alignment = Pos.Center
          padding = Insets(INFO_RULES_LEGEND_TOP_PADDING, 0, 0, 0)
          children = Seq(
            legendItem("\u2666", "Elixir"),
            legendItem("\u2665", "Health"),
            legendItem("\u2716", "Damage"),
            legendItem("\u2192", "Range")
          )
      )

    new VBox:
      alignment = Pos.Center
      children = Seq(
        new VBox:
          spacing = INFO_RULES_BOX_SPACING
          alignment = Pos.TopCenter
          prefWidth = INFO_RULES_BOX_WIDTH
          prefHeight = INFO_RULES_BOX_HEIGHT
          maxWidth = INFO_RULES_BOX_WIDTH
          maxHeight = INFO_RULES_BOX_HEIGHT
          style = createRulesBoxStyle()
          children = Seq(
            createGoldTitle("Game Rules", INFO_RULES_TITLE_FONT_SIZE),
            rulesContent
          )
      )

  private def createWizardsView(): GridPane =
    val cards = Seq(
      ("Generator", "", "Produces elixir", "/wizard/generator.png",
        Seq(("\u2666", GENERATOR_WIZARD_COST.toString), ("\u2665", GENERATOR_WIZARD_HEALTH.toString))),
      ("Wind", "", "Base attack", "/wizard/wind.png",
        Seq(("\u2666", WIND_WIZARD_COST.toString), ("\u2665", WIND_WIZARD_HEALTH.toString), ("\u2716", WIND_WIZARD_ATTACK_DAMAGE.toString), ("\u2192", WIND_WIZARD_RANGE.toString))),
      ("Fire", "", "Burns enemies", "/wizard/fire.png",
        Seq(("\u2666", FIRE_WIZARD_COST.toString), ("\u2665", FIRE_WIZARD_HEALTH.toString), ("\u2716", FIRE_WIZARD_ATTACK_DAMAGE.toString), ("\u2192", FIRE_WIZARD_RANGE.toString))),
      ("Ice", "", "Slows enemies", "/wizard/ice.png",
        Seq(("\u2666", ICE_WIZARD_COST.toString), ("\u2665", ICE_WIZARD_HEALTH.toString), ("\u2716", ICE_WIZARD_ATTACK_DAMAGE.toString), ("\u2192", ICE_WIZARD_RANGE.toString))),
      ("Barrier", "", "Blocks damage", "/wizard/barrier.png",
        Seq(("\u2666", BARRIER_WIZARD_COST.toString), ("\u2665", BARRIER_WIZARD_HEALTH.toString)))
    )
    createGrid(cards, cols = INFO_CARD_COLUMNS)

  private def createTrollsView(): GridPane =
    val cards = Seq(
      ("Base", "", "Standard troll", "/troll/BaseTroll.png",
        Seq(("\u2665", BASE_TROLL_HEALTH.toString), ("\u2716", BASE_TROLL_DAMAGE.toString), ("\u2192", BASE_TROLL_RANGE.toString))),
      ("Warrior", "", "Slow but tough", "/troll/WarriorTroll.png",
        Seq(("\u2665", WARRIOR_TROLL_HEALTH.toString), ("\u2716", WARRIOR_TROLL_DAMAGE.toString), ("\u2192", WARRIOR_TROLL_RANGE.toString))),
      ("Assassin", "", "Fast killer", "/troll/Assassin.png",
        Seq(("\u2665", ASSASSIN_TROLL_HEALTH.toString), ("\u2716", ASSASSIN_TROLL_DAMAGE.toString), ("\u2192", ASSASSIN_TROLL_RANGE.toString))),
      ("Thrower", "", "Ranged attacker", "/troll/ThrowerTroll.png",
        Seq(("\u2665", THROWER_TROLL_HEALTH.toString), ("\u2716", THROWER_TROLL_DAMAGE.toString), ("\u2192", THROWER_TROLL_RANGE.toString)))
    )
    createGrid(cards, cols = INFO_CARD_COLUMNS)

  private def baseRuleStyle: String =
    s"-fx-font-family: Arial; -fx-font-size: $INFO_RULES_TEXT_FONT_SIZE; -fx-fill: white;"