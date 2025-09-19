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
    lazy val wizardsBox = createBoxWithNavigation(createWizardsView(), contentArea, rulesView)
    lazy val trollsBox  = createBoxWithNavigation(createTrollsView(), contentArea, rulesView)

    contentArea.children = Seq(rulesView)

    val buttonConfigs = Map(
      "rules"   -> ButtonConfig("Rules", 180, 80, 20, "Times New Roman"),
      "wizards" -> ButtonConfig("Wizards", 180, 80, 20, "Times New Roman"),
      "trolls"  -> ButtonConfig("Trolls", 180, 80, 20, "Times New Roman")
    )

    val rulesButton   = createStyledButton(buttonConfigs("rules"))   { contentArea.children = Seq(rulesView) }
    val wizardsButton = createStyledButton(buttonConfigs("wizards")) { contentArea.children = Seq(wizardsBox) }
    val trollsButton  = createStyledButton(buttonConfigs("trolls"))  { contentArea.children = Seq(trollsBox) }

    val topBar = new HBox {
      spacing = 20
      alignment = Pos.Center
      padding = Insets(20)
      children = Seq(rulesButton, wizardsButton, trollsButton)
    }

    val bottomBar = new BorderPane {
      padding = Insets(PADDING_MENU)
      left = createStyledButton(ButtonConfig("Main Menu", 150, 80, 18, "Times New Roman")) {
        ViewController.showMainMenu()
      }
      right = createStyledButton(ButtonConfig("Exit", 150, 80, 18, "Times New Roman")) {
        sys.exit(0)
      }
    }

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

  private def createBoxWithNavigation(grid: GridPane, contentArea: StackPane, rulesView: VBox): VBox =
    new VBox {
      spacing = 20
      alignment = Pos.TopCenter
      children = Seq(
        grid,
        navigationBar(contentArea, rulesView)
      )
    }

  private def navigationBar(contentArea: StackPane, rulesView: VBox): HBox =
    new HBox {
      spacing = 15
      alignment = Pos.Center
      children = Seq(
        createStyledButton(ButtonConfig("Rules", 130, 60, 16, "Times New Roman")) {
          contentArea.children = Seq(rulesView)
        },
        createStyledButton(ButtonConfig("Main Menu", 160, 60, 16, "Times New Roman")) {
          ViewController.showMainMenu()
        }
      )
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
    
  private def createGrid(cards: Seq[(String,String,String,String,Seq[(String,String)])], cols: Int = 3): GridPane =
    val grid = new GridPane {
      hgap = 20
      vgap = 20
      alignment = Pos.Center
    }
    for ((card, idx) <- cards.zipWithIndex)
      val (name, stat, ability, imagePath, icons) = card
      grid.add(createCard(name, stat, ability, imagePath, icons), idx % cols, idx / cols)
    grid

  private def createCard(name: String, stat: String, ability: String, imagePath: String, icons: Seq[(String,String)]): VBox =
    val imageView = createImageView(imagePath, 100) match 
      case Right(img) =>
        img.fitWidth = 100
        img.fitHeight = 100
        img.preserveRatio = true
        img
      case Left(_) => new ImageView()
    
    val statBox = new HBox {
      spacing = 8
      alignment = Pos.Center
      children = icons.map { case (symbol,value) =>
        new VBox {
          spacing = 2
          alignment = Pos.Center
          children = Seq(
            new Text(symbol) {
              font = Font.font("Arial", FontWeight.Bold, 16)
              fill = Color.White
            },
            new Text(value) {
             font = Font.font("Arial", 14)
             fill = Color.LightGray 
            }
          )
        }
      }
    }
    
    val abilityBox = new Text(ability) {
      font = Font.font("Arial", FontWeight.Normal, 13)
      fill = Color.LightBlue
      wrappingWidth = 160
      textAlignment = TextAlignment.Center
    }

    new VBox {
      spacing = 8
      alignment = Pos.TopCenter
      prefWidth = 180
      prefHeight = 230
      style = """-fx-background-color: rgba(0,0,0,0.80);
                 -fx-background-radius: 15;
                 -fx-padding: 10;
                 -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 8,0,0,3);"""
      children = Seq(
        new Text(name) {
          font = Font.font("Times New Roman", FontWeight.Bold, 18)
          fill = Color.Gold
        },
        imageView,
        statBox,
        abilityBox
      )
    }

  private def createRulesView(): VBox =
    new VBox {
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
  
  private def createWizardsView(): GridPane =
    val cards = Seq(
      ("Generator", "♦ 5   ♥ 20", "Produces elixir", "/generator.png", Seq(("\u2666","5"),("\u2665","20"))),
      ("Wind",      "♦ 6 ♥ 15 ✖ 8 → 3", "Pushes enemies", "/wind.png", Seq(("\u2666","6"),("\u2665","15"),("\u2716","8"),("\u2192","3"))),
      ("Fire",      "♦ 7 ♥ 12 ✖ 12 → 3", "Burns enemies", "/fire.png", Seq(("\u2666","7"),("\u2665","12"),("\u2716","12"),("\u2192","3"))),
      ("Ice",       "♦ 6 ♥ 10 ✖ 10 → 3", "Slows enemies", "/ice.png", Seq(("\u2666","6"),("\u2665","10"),("\u2716","10"),("\u2192","3"))),
      ("Barrier",   "♦ 8 ♥ 30", "Blocks damage", "/barrier.png", Seq(("\u2666","8"),("\u2665","30")))
    )
    createGrid(cards, cols = 3)
  
  private def createTrollsView(): GridPane =
    val cards = Seq(
      ("Base",     "♥ 50   ✖ 6 → 2", "Main target", "/base.png", Seq(("\u2665","50"),("\u2716","6"),("\u2192","2"))),
      ("Warrior",  "♥ 40   ✖ 8 → 2", "Slow but tough", "/warriorTroll.png", Seq(("\u2665","40"),("\u2716","8"),("\u2192","2"))),
      ("Assassin", "♥ 20   ✖ 15 → 4","Fast killer", "/assassin.png", Seq(("\u2665","20"),("\u2716","15"),("\u2192","4"))),
      ("Thrower",  "♥ 25   ✖ 10 → 3","Ranged", "/thrower.png", Seq(("\u2665","25"),("\u2716","10"),("\u2192","3")))
    )
    createGrid(cards, cols = 3)
  
  private def baseRuleStyle: String =
    "-fx-font-family: Arial; -fx-font-size: 16; -fx-fill: white;"
}