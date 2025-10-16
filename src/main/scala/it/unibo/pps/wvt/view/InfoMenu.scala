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
import it.unibo.pps.wvt.view.ButtonFactory.Presets._
import it.unibo.pps.wvt.view.ImageFactory._
import it.unibo.pps.wvt.utilities.GamePlayConstants._

object InfoMenu:

  def apply(): Parent =
    lazy val backgroundImage = createBackgroundView("/main_menu.jpg", MENU_SCALE_FACTOR).getOrElse(new ImageView())
    val contentArea          = createContentArea()
    lazy val rulesView       = createRulesView()
    lazy val wizardsView     = createWizardsView()
    lazy val trollsView      = createTrollsView()

    contentArea.children = Seq(rulesView)

    val (rulesButton, wizardsButton, trollsButton) =
      createNavigationButtons(contentArea, rulesView, wizardsView, trollsView)
    val topBar    = createTopBar(rulesButton, wizardsButton, trollsButton)
    val bottomBar = createBottomBar()
    val layout    = createLayout(topBar, contentArea, bottomBar)

    new StackPane:
      children = Seq(backgroundImage, layout)

  private def createContentArea(): StackPane =
    new StackPane:
      prefHeight = INFO_CONTENT_AREA_HEIGHT
      minHeight = INFO_CONTENT_AREA_HEIGHT
      maxHeight = INFO_CONTENT_AREA_HEIGHT

  private def createLayout(topBar: HBox, contentArea: StackPane, bottomBar: BorderPane): BorderPane =
    new BorderPane:
      top = topBar
      center = new VBox:
        alignment = Pos.TopCenter
        padding = Insets(
          INFO_CENTER_PADDING_VERTICAL,
          INFO_CENTER_PADDING_HORIZONTAL,
          INFO_CENTER_PADDING_VERTICAL,
          INFO_CENTER_PADDING_HORIZONTAL
        )
        children = Seq(contentArea)
      bottom = bottomBar

  private def createNavigationButtons(
      contentArea: StackPane,
      rulesView: VBox,
      wizardsView: GridPane,
      trollsView: GridPane
  ): (Button, Button, Button) =
    val rulesButton   = createStyledButton(navButtonPreset("Rules")) {}
    val wizardsButton = createStyledButton(navButtonPreset("Wizards")) {}
    val trollsButton  = createStyledButton(navButtonPreset("Trolls")) {}

    val updateStyles: String => Unit = activeButton =>
      rulesButton.opacity = buttonOpacity(activeButton, "rules")
      wizardsButton.opacity = buttonOpacity(activeButton, "wizards")
      trollsButton.opacity = buttonOpacity(activeButton, "trolls")

    rulesButton.onAction = _ =>
      contentArea.children = Seq(rulesView)
      updateStyles("rules")

    wizardsButton.onAction = _ =>
      contentArea.children = Seq(wizardsView)
      updateStyles("wizards")

    trollsButton.onAction = _ =>
      contentArea.children = Seq(trollsView)
      updateStyles("trolls")

    updateStyles("rules")
    (rulesButton, wizardsButton, trollsButton)

  private def buttonOpacity(activeButton: String, buttonName: String): Double =
    activeButton == buttonName match
      case true  => INFO_BUTTON_ACTIVE_OPACITY
      case false => INFO_BUTTON_INACTIVE_OPACITY

  private def createTopBar(rulesButton: Button, wizardsButton: Button, trollsButton: Button): HBox =
    new HBox:
      spacing = INFO_TOP_BAR_SPACING
      alignment = Pos.Center
      padding = Insets(INFO_TOP_BAR_PADDING)
      children = Seq(rulesButton, wizardsButton, trollsButton)

  private def createBottomBar(): BorderPane =
    new BorderPane:
      padding = Insets(PADDING_MENU)
      left = createStyledButton(smallButtonPreset("Main Menu"))(handleAction(BackToMenu))
      right = createStyledButton(smallButtonPreset("Exit"))(handleAction(ExitGame))

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

  private def createGrid(
      cards: Seq[(String, String, String, String, Seq[(String, String)])],
      cols: Int = INFO_CARD_COLUMNS
  ): GridPane =
    val grid = new GridPane:
      hgap = INFO_CARD_GRID_GAP
      vgap = INFO_CARD_GRID_GAP
      alignment = Pos.Center

    cards.zipWithIndex.foreach:
      case ((name, stat, ability, imagePath, icons), idx) =>
        grid.add(createCard(name, stat, ability, imagePath, icons), idx % cols, idx / cols)

    grid

  private def createCard(
      name: String,
      stat: String,
      ability: String,
      imagePath: String,
      icons: Seq[(String, String)]
  ): VBox =
    val imageView  = loadCardImage(imagePath)
    val statBox    = createStatBox(icons)
    val abilityBox = createAbilityBox(ability)

    new VBox:
      spacing = INFO_CARD_SPACING
      alignment = Pos.TopCenter
      prefWidth = INFO_CARD_WIDTH
      prefHeight = INFO_CARD_HEIGHT
      style = createCardStyle
      children = Seq(createGoldTitle(name), imageView, statBox, abilityBox)

  private def loadCardImage(imagePath: String): ImageView =
    createImageView(imagePath, INFO_CARD_IMAGE_SIZE).fold(
      _ => new ImageView(),
      img =>
        img.fitWidth = INFO_CARD_IMAGE_SIZE
        img.fitHeight = INFO_CARD_IMAGE_SIZE
        img.preserveRatio = true
        img
    )

  private def createStatBox(icons: Seq[(String, String)]): HBox =
    new HBox:
      spacing = INFO_CARD_STAT_BOX_SPACING
      alignment = Pos.Center
      children = icons.map:
        case (symbol, value) => createStatText(symbol, value)

  private def createAbilityBox(ability: String): Text =
    new Text(ability):
      font = Font.font("Arial", FontWeight.Normal, INFO_CARD_ABILITY_FONT_SIZE)
      fill = Color.LightBlue
      wrappingWidth = INFO_CARD_ABILITY_WIDTH
      textAlignment = TextAlignment.Center

  private lazy val createCardStyle: String =
    s"""-fx-background-color: rgba(0,0,0,$INFO_CARD_BG_OPACITY);
       -fx-background-radius: $INFO_CARD_BORDER_RADIUS;
       -fx-padding: $INFO_CARD_PADDING;
       -fx-effect: dropshadow(gaussian, rgba(0,0,0,$INFO_CARD_SHADOW_OPACITY), $INFO_CARD_SHADOW_RADIUS,0,0,$INFO_CARD_SHADOW_OFFSET);"""

  private lazy val createRulesBoxStyle: String =
    s"""-fx-background-color: rgba(0,0,0,$INFO_RULES_BG_OPACITY);
       -fx-background-radius: $INFO_RULES_BORDER_RADIUS;
       -fx-padding: $INFO_RULES_PADDING;
       -fx-effect: dropshadow(gaussian, rgba(0,0,0,$INFO_RULES_SHADOW_OPACITY), $INFO_RULES_SHADOW_RADIUS,0,0,$INFO_RULES_SHADOW_OFFSET);"""

  private def createRulesView(): VBox =
    val rulesContent = createRulesContent()
    createRulesContainer(rulesContent)

  private def createRulesContent(): VBox =
    new VBox:
      alignment = Pos.TopCenter
      spacing = INFO_RULES_CONTENT_SPACING
      children = Seq(
        createRuleText(s"♦ You start with $INITIAL_ELIXIR elixir to buy wizards"),
        createRuleText("🎯 Click empty cells to place wizards and defend against trolls"),
        createRuleText("🏰 Generators create elixir - place them first!"),
        createRuleText("⚔ Wizards attack trolls automatically when in range"),
        createLegendBox()
      )

  private def createRuleText(text: String): Text =
    new Text(text):
      style = baseRuleStyle

  private def createLegendBox(): HBox =
    new HBox:
      spacing = INFO_RULES_LEGEND_SPACING
      alignment = Pos.Center
      padding = Insets(INFO_RULES_LEGEND_TOP_PADDING, 0, 0, 0)
      children = Seq(
        legendItem("♦", "Elixir"),
        legendItem("♥", "Health"),
        legendItem("✖", "Damage"),
        legendItem("→", "Range")
      )

  private def createRulesContainer(rulesContent: VBox): VBox =
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
          style = createRulesBoxStyle
          children = Seq(
            createGoldTitle("Game Rules", INFO_RULES_TITLE_FONT_SIZE),
            rulesContent
          )
      )

  private def createWizardsView(): GridPane =
    val cards = Seq(
      (
        "Generator",
        "",
        "Produces elixir",
        "/wizard/generator.png",
        Seq(("♦", GENERATOR_WIZARD_COST.toString), ("♥", GENERATOR_WIZARD_HEALTH.toString))
      ),
      (
        "Wind",
        "",
        "Base attack",
        "/wizard/wind.png",
        Seq(
          ("♦", WIND_WIZARD_COST.toString),
          ("♥", WIND_WIZARD_HEALTH.toString),
          ("✖", WIND_WIZARD_ATTACK_DAMAGE.toString),
          ("→", WIND_WIZARD_RANGE.toString)
        )
      ),
      (
        "Fire",
        "",
        "Burns enemies",
        "/wizard/fire.png",
        Seq(
          ("♦", FIRE_WIZARD_COST.toString),
          ("♥", FIRE_WIZARD_HEALTH.toString),
          ("✖", FIRE_WIZARD_ATTACK_DAMAGE.toString),
          ("→", FIRE_WIZARD_RANGE.toString)
        )
      ),
      (
        "Ice",
        "",
        "Slows enemies",
        "/wizard/ice.png",
        Seq(
          ("♦", ICE_WIZARD_COST.toString),
          ("♥", ICE_WIZARD_HEALTH.toString),
          ("✖", ICE_WIZARD_ATTACK_DAMAGE.toString),
          ("→", ICE_WIZARD_RANGE.toString)
        )
      ),
      (
        "Barrier",
        "",
        "Blocks damage",
        "/wizard/barrier.png",
        Seq(("♦", BARRIER_WIZARD_COST.toString), ("♥", BARRIER_WIZARD_HEALTH.toString))
      )
    )
    createGrid(cards, cols = INFO_CARD_COLUMNS)

  private def createTrollsView(): GridPane =
    val cards = Seq(
      (
        "Base",
        "",
        "Standard troll",
        "/troll/BaseTroll.png",
        Seq(("♥", BASE_TROLL_HEALTH.toString), ("✖", BASE_TROLL_DAMAGE.toString), ("→", BASE_TROLL_RANGE.toString))
      ),
      (
        "Warrior",
        "",
        "Slow but tough",
        "/troll/WarriorTroll.png",
        Seq(
          ("♥", WARRIOR_TROLL_HEALTH.toString),
          ("✖", WARRIOR_TROLL_DAMAGE.toString),
          ("→", WARRIOR_TROLL_RANGE.toString)
        )
      ),
      (
        "Assassin",
        "",
        "Fast killer",
        "/troll/Assassin.png",
        Seq(
          ("♥", ASSASSIN_TROLL_HEALTH.toString),
          ("✖", ASSASSIN_TROLL_DAMAGE.toString),
          ("→", ASSASSIN_TROLL_RANGE.toString)
        )
      ),
      (
        "Thrower",
        "",
        "Ranged attacker",
        "/troll/ThrowerTroll.png",
        Seq(
          ("♥", THROWER_TROLL_HEALTH.toString),
          ("✖", THROWER_TROLL_DAMAGE.toString),
          ("→", THROWER_TROLL_RANGE.toString)
        )
      )
    )
    createGrid(cards, cols = INFO_CARD_COLUMNS)

  private lazy val baseRuleStyle: String =
    s"-fx-font-family: Arial; -fx-font-size: $INFO_RULES_TEXT_FONT_SIZE; -fx-fill: white;"
