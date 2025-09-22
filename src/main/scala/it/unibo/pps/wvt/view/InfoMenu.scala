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

object InfoMenu {

  def apply(): Parent =
    lazy val backgroundImage = createBackgroundView("/main_menu.jpg", MENU_SCALE_FACTOR).getOrElse(new ImageView())
    val contentArea = new StackPane()

    lazy val rulesView  = createRulesView()
    lazy val wizardsView = createWizardsView()
    lazy val trollsView = createTrollsView()

    contentArea.children = Seq(rulesView)

    val (rulesButton, wizardsButton, trollsButton) = createNavigationButtons(contentArea, rulesView, wizardsView, trollsView)
    val topBar = createTopBar(rulesButton, wizardsButton, trollsButton)
    val bottomBar = createBottomBar()

    val layout = new BorderPane {
      top = topBar
      center = new VBox {
        alignment = Pos.TopCenter
        padding = Insets(10, 30, 10, 30)
        children = Seq(contentArea)
      }
      bottom = bottomBar
    }

    new StackPane {
      children = Seq(backgroundImage, layout)
    }

  private def createNavigationButtons(contentArea: StackPane, rulesView: VBox, wizardsView: GridPane, trollsView: GridPane) = {
    val buttonConfigs = Map(
      "rules"   -> ButtonConfig("Rules", 140, 60, 16, "Times New Roman"),
      "wizards" -> ButtonConfig("Wizards", 140, 60, 16, "Times New Roman"),
      "trolls"  -> ButtonConfig("Trolls", 140, 60, 16, "Times New Roman")
    )

    val rulesButton = createStyledButton(buttonConfigs("rules")) { /* azione definita dopo */ }
    val wizardsButton = createStyledButton(buttonConfigs("wizards")) { /* azione definita dopo */ }
    val trollsButton = createStyledButton(buttonConfigs("trolls")) { /* azione definita dopo */ }

    def updateButtonStyles(activeButton: String): Unit = {
      rulesButton.opacity = if (activeButton == "rules") 1.0 else 0.7
      wizardsButton.opacity = if (activeButton == "wizards") 1.0 else 0.7
      trollsButton.opacity = if (activeButton == "trolls") 1.0 else 0.7
    }

    rulesButton.onAction = _ => {
      contentArea.children = Seq(rulesView)
      updateButtonStyles("rules")
    }

    wizardsButton.onAction = _ => {
      contentArea.children = Seq(wizardsView)
      updateButtonStyles("wizards")
    }

    trollsButton.onAction = _ => {
      contentArea.children = Seq(trollsView)
      updateButtonStyles("trolls")
    }

    updateButtonStyles("rules")
    (rulesButton, wizardsButton, trollsButton)
  }

  private def createTopBar(rulesButton: Button, wizardsButton: Button, trollsButton: Button): HBox = {
    new HBox {
      spacing = 20
      alignment = Pos.Center
      padding = Insets(20)
      children = Seq(rulesButton, wizardsButton, trollsButton)
    }
  }

  private def createBottomBar(): BorderPane = {
    new BorderPane {
      padding = Insets(PADDING_MENU)
      left = createStyledButton(ButtonConfig("Main Menu", 150, 80, 15, "Times New Roman"))(handleAction(BackToMenu))
      right = createStyledButton(ButtonConfig("Exit", 150, 80, 15, "Times New Roman"))(handleAction(ExitGame))
    }
  }

  private def createGoldTitle(text: String, fontSize: Int = 16): Text = {
    new Text(text) {
      font = Font.font("Times New Roman", FontWeight.Bold, fontSize)
      fill = Color.Gold
    }
  }

  private def createStatText(symbol: String, value: String): VBox = {
    new VBox {
      spacing = 2
      alignment = Pos.Center
      children = Seq(
        new Text(symbol) {
          font = Font.font("Arial", FontWeight.Bold, 14)
          fill = Color.White
        },
        new Text(value) {
          font = Font.font("Arial", 12)
          fill = Color.LightGray
        }
      )
    }
  }

  private def legendItem(symbol: String, textLabel: String): VBox =
    new VBox {
      spacing = 4
      alignment = Pos.Center
      children = Seq(
        new Text(symbol) {
          font = Font.font("Arial", FontWeight.Bold, 18)
          fill = Color.White
        },
        new Text(textLabel) {
          font = Font.font("Arial", 12)
          fill = Color.LightGray
        }
      )
    }

  private def createGrid(cards: Seq[(String,String,String,String,Seq[(String,String)])], cols: Int = 4): GridPane =
    val grid = new GridPane {
      hgap = 15
      vgap = 15
      alignment = Pos.Center
    }
    for ((card, idx) <- cards.zipWithIndex)
      val (name, stat, ability, imagePath, icons) = card
      grid.add(createCard(name, stat, ability, imagePath, icons), idx % cols, idx / cols)
    grid

  private def createCard(name: String, stat: String, ability: String, imagePath: String, icons: Seq[(String,String)]): VBox =
    val imageView = createImageView(imagePath, 80) match
      case Right(img) =>
        img.fitWidth = 80
        img.fitHeight = 80
        img.preserveRatio = true
        img
      case Left(_) => new ImageView()

    val statBox = new HBox {
      spacing = 6
      alignment = Pos.Center
      children = icons.map { case (symbol, value) => createStatText(symbol, value) }
    }

    val abilityBox = new Text(ability) {
      font = Font.font("Arial", FontWeight.Normal, 11)
      fill = Color.LightBlue
      wrappingWidth = 140
      textAlignment = TextAlignment.Center
    }

    new VBox {
      spacing = 6
      alignment = Pos.TopCenter
      prefWidth = 150
      prefHeight = 190
      style = """-fx-background-color: rgba(0,0,0,0.80);
                 -fx-background-radius: 15;
                 -fx-padding: 8;
                 -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 6,0,0,2);"""
      children = Seq(
        createGoldTitle(name),
        imageView,
        statBox,
        abilityBox
      )
    }

  private def createRulesView(): VBox =
    val rulesContent = new VBox {
      alignment = Pos.TopCenter
      spacing = 12
      children = Seq(
        new Text(s"\u2666 You start with $INITIAL_ELIXIR elixir to buy wizards") { style = baseRuleStyle },
        new Text("🎯 Click empty cells to place wizards and defend against trolls") { style = baseRuleStyle },
        new Text("🏰 Generators create elixir - place them first!") { style = baseRuleStyle },
        new Text("⚔ Wizards attack trolls automatically when in range") { style = baseRuleStyle },
        new HBox {
          spacing = 15
          alignment = Pos.Center
          padding = Insets(10,0,0,0)
          children = Seq(
            legendItem("\u2666","Elixir"),
            legendItem("\u2665","Health"),
            legendItem("\u2716","Damage"),
            legendItem("\u2192","Range")
          )
        }
      )
    }

    new VBox {
      alignment = Pos.Center
      children = Seq(
        new VBox {
          spacing = 15
          alignment = Pos.TopCenter
          prefWidth = 350
          prefHeight = 280
          maxWidth = 350
          maxHeight = 280
          style = """-fx-background-color: rgba(0,0,0,0.85);
                     -fx-background-radius: 20;
                     -fx-padding: 20;
                     -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 10,0,0,4);"""
          children = Seq(
            createGoldTitle("Game Rules", 24),
            rulesContent
          )
        }
      )
    }

  private def createWizardsView(): GridPane =
    val cards = Seq(
      ("Generator", "♦ 5   ♥ 20", "Produces elixir", "/generator.png", Seq(("\u2666","5"),("\u2665","20"))),
      ("Wind",      "♦ 6 ♥ 15 ✖ 8 → 3", "Base", "/wind.png", Seq(("\u2666","6"),("\u2665","15"),("\u2716","8"),("\u2192","3"))),
      ("Fire",      "♦ 7 ♥ 12 ✖ 12 → 3", "Burns enemies", "/fire.png", Seq(("\u2666","7"),("\u2665","12"),("\u2716","12"),("\u2192","3"))),
      ("Ice",       "♦ 6 ♥ 10 ✖ 10 → 3", "Slows enemies", "/ice.png", Seq(("\u2666","6"),("\u2665","10"),("\u2716","10"),("\u2192","3"))),
      ("Barrier",   "♦ 8 ♥ 30", "Blocks damage", "/barrier.png", Seq(("\u2666","8"),("\u2665","30")))
    )
    createGrid(cards, cols = 4)

  private def createTrollsView(): GridPane =
    val cards = Seq(
      ("Base",     "♥ 50   ✖ 6 → 2", "Base", "/base.png", Seq(("\u2665","50"),("\u2716","6"),("\u2192","2"))),
      ("Warrior",  "♥ 40   ✖ 8 → 2", "Slow but tough", "/warriorTroll.png", Seq(("\u2665","40"),("\u2716","8"),("\u2192","2"))),
      ("Assassin", "♥ 20   ✖ 15 → 4","Fast killer", "/assassin.png", Seq(("\u2665","20"),("\u2716","15"),("\u2192","4"))),
      ("Thrower",  "♥ 25   ✖ 10 → 3","Ranged", "/thrower.png", Seq(("\u2665","25"),("\u2716","10"),("\u2192","3")))
    )
    createGrid(cards, cols = 4)

  private def baseRuleStyle: String =
    "-fx-font-family: Arial; -fx-font-size: 16; -fx-fill: white;"
}