package it.unibo.pps.wvt.utilities

object GameConstants {
  val TARGET_FPS: Int = 60
  val FRAME_TIME_NANOS: Long = 1_000_000_000L / TARGET_FPS
  val FRAME_TIME_MICROS: Long = 1000L / TARGET_FPS
}

object GamePlayConstants {
  // Elixir generation parameters
  val INITIAL_ELIXIR: Int = 200
  val PERIODIC_ELIXIR: Int = 100
  val ELIXIR_GENERATION_INTERVAL: Long = 10000

  // Troll spawn parameters
  val INITIAL_SPAWN_INTERVAL: Long = 2000

  // Troll parameters
  val BASE_TROLL_HEALTH: Int = 100
  val BASE_TROLL_SPEED: Int = 2
  val BASE_TROLL_DAMAGE: Int = 20
  val BASE_TROLL_REWARD: Int = 25
  
  val WARRIOR_TROLL_HEALTH: Int = 200
  val WARRIOR_TROLL_SPEED: Int = 1
  val WARRIOR_TROLL_DAMAGE: Int = 40
  val WARRIOR_TROLL_REWARD: Int = 75
  
  val ASSASSIN_TROLL_HEALTH: Int = 50
  val ASSASSIN_TROLL_SPEED: Int = 3
  val ASSASSIN_TROLL_DAMAGE: Int = 60
  val ASSASSIN_TROLL_REWARD: Int = 100
  
  val THROWER_TROLL_HEALTH: Int = 75
  val THROWER_TROLL_SPEED: Int = 2
  val THROWER_TROLL_DAMAGE: Int = 30
  val THROWER_TROLL_REWARD: Int = 125
  
  val GENERATOR_WIZARD_HEALTH: Int = 150
  val GENERATOR_WIZARD_COST: Int = 100
  val GENERATOR_WIZARD_ATTACK_DAMAGE: Int = 0
  val GENERATOR_WIZARD_RANGE: Int = 0
  val GENERATOR_WIZARD_ELIXIR: Int = 25
  
  val WIND_WIZARD_HEALTH: Int = 100
  val WIND_WIZARD_COST: Int = 150
  val WIND_WIZARD_ATTACK_DAMAGE: Int = 25
  val WIND_WIZARD_RANGE: Int = 3
  
  val BARRIER_WIZARD_HEALTH: Int = 300
  val BARRIER_WIZARD_COST: Int = 200
  val BARRIER_WIZARD_ATTACK_DAMAGE: Int = 0
  val BARRIER_WIZARD_RANGE: Int = 0
  
  val FIRE_WIZARD_HEALTH: Int = 80
  val FIRE_WIZARD_COST: Int = 200
  val FIRE_WIZARD_ATTACK_DAMAGE: Int = 50
  val FIRE_WIZARD_RANGE: Int = 2
  
  val ICE_WIZARD_HEALTH: Int = 90
  val ICE_WIZARD_COST: Int = 250
  val ICE_WIZARD_ATTACK_DAMAGE: Int = 30
  val ICE_WIZARD_RANGE: Int = 3
  
  val PROJECTILE_SPEED: Int = 3
}

object ViewConstants {
  //Grid dimensions
  val GRID_ROWS: Int = 5
  val GRID_COLS: Int = 9

  val GRID_OFFSET_X: Double = 562
  val GRID_OFFSET_Y: Double = 163
  val CELL_WIDTH: Int = 66
  val CELL_HEIGHT: Int = 77
  val CELL_OPACITY: Double = 0.3
  
  // Window's and menu's objects dimensions
  val MENU_SCALE_FACTOR = 0.7
  val GAME_MAP_SCALE_FACTOR = 0.4
  val TITLE_SCALE_FACTOR = 0.4
  
  val PADDING_MENU = 15
}