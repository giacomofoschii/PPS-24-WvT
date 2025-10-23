package it.unibo.pps.wvt.utilities

/** Object containing game frame constants. */
object GameConstants:
  private val TARGET_FPS: Int = 60
  val FRAME_TIME_NANOS: Long  = 1_000_000_000L / TARGET_FPS
  val FRAME_TIME_MILLIS: Long = 1000L / TARGET_FPS

/** Object containing constants related to game play mechanics. */
object GamePlayConstants:
  // Elixir generation parameters
  val INITIAL_ELIXIR: Int              = 200
  val PERIODIC_ELIXIR: Int             = 100
  val ELIXIR_GENERATION_INTERVAL: Long = 10000
  val MAX_ELIXIR: Int                  = 1000

  // Troll spawn parameters
  val INITIAL_SPAWN_INTERVAL: Long = 3000L
  val SPAWN_INTERVAL: Long         = 10000L
  val MAX_TROLLS_PER_WAVE_1: Int   = 10
  val MIN_SPAWN_INTERVAL           = 2000L
  val INTERVAL_DECREASE_PER_WAVE   = 150L
  val BATCH_INTERVAL               = 1500L

  val BASE_BATCH_SIZE = 1 // It starts with 1 troll per batch
  val MAX_BATCH_SIZE  = 3 // Maximum 3 trolls per batch

  // Multipliers per wave
  val HEALTH_INCREASE_PER_WAVE = 0.10
  val SPEED_INCREASE_PER_WAVE  = 0.08
  val DAMAGE_INCREASE_PER_WAVE = 0.11

  // Troll parameters
  val BASE_TROLL_HEALTH: Int    = 100
  val BASE_TROLL_SPEED: Double  = 0.10
  val BASE_TROLL_DAMAGE: Int    = 20
  val BASE_TROLL_RANGE: Double  = 1.0
  val BASE_TROLL_COOLDOWN: Long = 1000L
  val BASE_TROLL_REWARD: Int    = 25

  val WARRIOR_TROLL_HEALTH: Int    = 130
  val WARRIOR_TROLL_SPEED: Double  = 0.15
  val WARRIOR_TROLL_DAMAGE: Int    = 30
  val WARRIOR_TROLL_RANGE: Double  = 0.5
  val WARRIOR_TROLL_COOLDOWN: Long = 1500L
  val WARRIOR_TROLL_REWARD: Int    = 75

  val ASSASSIN_TROLL_HEALTH: Int    = 70
  val ASSASSIN_TROLL_SPEED: Double  = 0.2
  val ASSASSIN_TROLL_DAMAGE: Int    = 60
  val ASSASSIN_TROLL_RANGE: Double  = 1.5
  val ASSASSIN_TROLL_COOLDOWN: Long = 800L
  val ASSASSIN_TROLL_REWARD: Int    = 100

  val THROWER_TROLL_HEALTH: Int    = 40
  val THROWER_TROLL_SPEED: Double  = 0.10
  val THROWER_TROLL_DAMAGE: Int    = 10
  val THROWER_TROLL_RANGE: Double  = 5.0
  val THROWER_TROLL_COOLDOWN: Long = 3000L
  val THROWER_TROLL_REWARD: Int    = 125

  // Wizard parameters
  val GENERATOR_WIZARD_HEALTH: Int            = 150
  val GENERATOR_WIZARD_COST: Int              = 100
  val GENERATOR_WIZARD_ELIXIR_PER_SECOND: Int = 25
  val GENERATOR_WIZARD_COOLDOWN: Long         = 10000L

  val WIND_WIZARD_HEALTH: Int        = 100
  val WIND_WIZARD_COST: Int          = 150
  val WIND_WIZARD_ATTACK_DAMAGE: Int = 25
  val WIND_WIZARD_RANGE: Double      = 3.0
  val WIND_WIZARD_COOLDOWN: Long     = 3000L

  val BARRIER_WIZARD_HEALTH: Int = 300
  val BARRIER_WIZARD_COST: Int   = 200

  val FIRE_WIZARD_HEALTH: Int        = 100
  val FIRE_WIZARD_COST: Int          = 250
  val FIRE_WIZARD_ATTACK_DAMAGE: Int = 50
  val FIRE_WIZARD_RANGE: Double      = 2.0
  val FIRE_WIZARD_COOLDOWN: Long     = 2500L

  val ICE_WIZARD_HEALTH: Int        = 150
  val ICE_WIZARD_COST: Int          = 200
  val ICE_WIZARD_ATTACK_DAMAGE: Int = 25
  val ICE_WIZARD_RANGE: Double      = 2.5
  val ICE_WIZARD_COOLDOWN: Long     = 4000L

  val PROJECTILE_SPEED: Double = 0.5

/** Object containing constants related to the game view and UI. */
object ViewConstants:
  val GRID_ROWS: Int = 5
  val GRID_COLS: Int = 9

  val GRID_OFFSET_X: Double = 562
  val GRID_OFFSET_Y: Double = 163

  val CELL_WIDTH: Int      = 66
  val CELL_HEIGHT: Int     = 77
  val CELL_OPACITY: Double = 0.3

  val MENU_SCALE_FACTOR          = 0.7
  val IN_GAME_MENU_SCALE_FACTOR  = 0.5
  val GAME_MAP_SCALE_FACTOR      = 0.4
  val TITLE_SCALE_FACTOR         = 0.4
  val IN_GAME_TITLE_SCALE_FACTOR = 0.5

  val PAUSE_BUTTON_WIDTH     = 175
  val PAUSE_BUTTON_HEIGHT    = 100
  val PAUSE_BUTTON_FONT_SIZE = 18
  val PAUSE_BUTTON_SPICING   = 20

  val PADDING_MENU = 15

  val HEALTH_BAR_WIDTH: Double = 40.0
  val HEALTH_BAR_OFFSET_Y: Int = -5

  val DEBOUNCE_MS: Long = 200L

  val SHOP_PANEL_WIDTH: Int                  = 250
  val SHOP_PANEL_TOP_PADDING: Int            = 120
  val SHOP_PANEL_SIDE_PADDING: Int           = 20
  val SHOP_PANEL_BOTTOM_PADDING: Int         = 20
  val SHOP_PANEL_BORDER_RADIUS: Int          = 20
  val SHOP_PANEL_SPACING: Int                = 16
  val SHOP_CONTENT_TOP_PADDING: Int          = 10
  val SHOP_BUTTON_WIDTH: Int                 = 200
  val SHOP_BUTTON_HEIGHT: Int                = 100
  val SHOP_BUTTON_FONT_SIZE: Int             = 20
  val SHOP_BUTTON_TOP_OFFSET: Int            = 30
  val SHOP_CARD_WIDTH: Int                   = 100
  val SHOP_CARD_HEIGHT: Int                  = 110
  val SHOP_CARD_SPACING: Int                 = 4
  val SHOP_CARD_IMAGE_SIZE: Int              = 50
  val SHOP_GRID_GAP: Int                     = 10
  val SHOP_CARD_COLUMNS: Int                 = 2
  val ELIXIR_FONT_SIZE: Int                  = 13
  val WIZARD_NAME_FONT_SIZE: Int             = 12
  val WIZARD_NAME_WIDTH: Int                 = 100
  val WIZARD_COST_FONT_SIZE: Int             = 11
  val SHOP_PANEL_X_POSITION: Int             = 10
  val SHOP_PANEL_Y_POSITION: Int             = 10
  val PAUSE_BUTTON_X_POSITION: Int           = 1050
  val SHOP_SHADOW_OPACITY: Double            = 0.7
  val SHOP_SHADOW_RADIUS: Int                = 8
  val SHOP_SHADOW_OFFSET_X: Int              = 2
  val SHOP_SHADOW_OFFSET_Y: Int              = 2
  val SHOP_PRIMARY_COLOR: String             = "#4CAF50"
  val SHOP_HOVER_COLOR: String               = "#66BB6A"
  val SHOP_DISABLED_COLOR: String            = "#666666"
  val SHOP_CARD_BG_R: Int                    = 40
  val SHOP_CARD_BG_G: Int                    = 40
  val SHOP_CARD_BG_B: Int                    = 40
  val SHOP_CARD_BG_OPACITY: Double           = 0.9
  val SHOP_CARD_DISABLED_OPACITY: Double     = 0.6
  val SHOP_CARD_HOVER_BG_R: Int              = 60
  val SHOP_CARD_HOVER_BG_G: Int              = 60
  val SHOP_CARD_HOVER_BG_B: Int              = 60
  val SHOP_CARD_HOVER_BG_OPACITY: Double     = 0.95
  val SHOP_CARD_BORDER_RADIUS: Int           = 6
  val SHOP_CARD_PADDING: Int                 = 12
  val SHOP_CARD_SHADOW_OPACITY: Double       = 0.5
  val SHOP_CARD_SHADOW_RADIUS: Int           = 3
  val SHOP_CARD_SHADOW_OFFSET_X: Int         = 1
  val SHOP_CARD_SHADOW_OFFSET_Y: Int         = 1
  val SHOP_CARD_HOVER_SHADOW_R: Int          = 74
  val SHOP_CARD_HOVER_SHADOW_G: Int          = 144
  val SHOP_CARD_HOVER_SHADOW_B: Int          = 226
  val SHOP_CARD_HOVER_SHADOW_OPACITY: Double = 0.6
  val SHOP_CARD_HOVER_SHADOW_RADIUS: Int     = 5
  val SHOP_CARD_BORDER_WIDTH_ACTIVE: Int     = 2
  val SHOP_CARD_BORDER_WIDTH_DISABLED: Int   = 1
  val SHOP_DISABLED_TEXT_COLOR: String       = "#999999"

  val INFO_CARD_BG_OPACITY: Double     = 0.80
  val INFO_CARD_BORDER_RADIUS: Int     = 15
  val INFO_CARD_PADDING: Int           = 6
  val INFO_CARD_SHADOW_OPACITY: Double = 0.65
  val INFO_CARD_SHADOW_RADIUS: Int     = 6
  val INFO_CARD_SHADOW_OFFSET: Int     = 2

  val INFO_RULES_BG_OPACITY: Double     = 0.85
  val INFO_RULES_BORDER_RADIUS: Int     = 20
  val INFO_RULES_PADDING: Int           = 20
  val INFO_RULES_SHADOW_OPACITY: Double = 0.7
  val INFO_RULES_SHADOW_RADIUS: Int     = 10
  val INFO_RULES_SHADOW_OFFSET: Int     = 4
  val INFO_RULES_TEXT_FONT_SIZE: Int    = 16

  val INFO_CONTENT_AREA_HEIGHT: Int        = 320
  val INFO_CENTER_PADDING_VERTICAL: Int    = 10
  val INFO_CENTER_PADDING_HORIZONTAL: Int  = 30
  val INFO_NAV_BUTTON_WIDTH: Int           = 140
  val INFO_NAV_BUTTON_HEIGHT: Int          = 60
  val INFO_NAV_BUTTON_FONT_SIZE: Int       = 16
  val INFO_BUTTON_ACTIVE_OPACITY: Double   = 1.0
  val INFO_BUTTON_INACTIVE_OPACITY: Double = 0.7
  val INFO_TOP_BAR_SPACING: Int            = 20
  val INFO_TOP_BAR_PADDING: Int            = 20
  val INFO_BOTTOM_BUTTON_WIDTH: Int        = 150
  val INFO_BOTTOM_BUTTON_HEIGHT: Int       = 80
  val INFO_BOTTOM_BUTTON_FONT_SIZE: Int    = 15
  val INFO_GOLD_TITLE_FONT_SIZE: Int       = 16
  val INFO_STAT_TEXT_SPACING: Int          = 2
  val INFO_STAT_SYMBOL_FONT_SIZE: Int      = 12
  val INFO_STAT_VALUE_FONT_SIZE: Int       = 10
  val INFO_LEGEND_SPACING: Int             = 4
  val INFO_LEGEND_SYMBOL_FONT_SIZE: Int    = 18
  val INFO_LEGEND_TEXT_FONT_SIZE: Int      = 12
  val INFO_CARD_COLUMNS: Int               = 4
  val INFO_CARD_GRID_GAP: Int              = 12
  val INFO_CARD_IMAGE_SIZE: Int            = 60
  val INFO_CARD_STAT_BOX_SPACING: Int      = 4
  val INFO_CARD_ABILITY_FONT_SIZE: Int     = 10
  val INFO_CARD_ABILITY_WIDTH: Int         = 120
  val INFO_CARD_SPACING: Int               = 4
  val INFO_CARD_WIDTH: Int                 = 130
  val INFO_CARD_HEIGHT: Int                = 160
  val INFO_RULES_CONTENT_SPACING: Int      = 12
  val INFO_RULES_LEGEND_SPACING: Int       = 15
  val INFO_RULES_LEGEND_TOP_PADDING: Int   = 10
  val INFO_RULES_BOX_SPACING: Int          = 15
  val INFO_RULES_BOX_WIDTH: Int            = 350
  val INFO_RULES_BOX_HEIGHT: Int           = 280
  val INFO_RULES_TITLE_FONT_SIZE: Int      = 24
