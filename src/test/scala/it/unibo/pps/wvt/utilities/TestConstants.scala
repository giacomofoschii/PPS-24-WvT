package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.ViewConstants.*

object TestConstants:
  // Time constants (in milliseconds)
  val SHORT_DELAY: Long           = 50L
  val MEDIUM_DELAY: Long          = 100L
  val LONG_DELAY: Long            = 200L
  val FPS_CALCULATION_DELAY: Long = 1100L
  val STANDARD_DELTA_TIME: Long   = 16L // ~60 FPS
  val EXPECTED_FRAME_TIME: Long   = 500L

  // Loop iterations
  val SMALL_ITERATION_COUNT: Int  = 3
  val MEDIUM_ITERATION_COUNT: Int = 10

  // Expected values
  val INITIAL_TIME: Long        = 0L
  val INITIAL_ENTITY_COUNT: Int = 0
  val INITIAL_FPS: Int          = 0

  // FPS testing
  val MIN_EXPECTED_FPS: Int       = 45
  val MAX_EXPECTED_FPS: Int       = 75
  val MAX_UPDATES_AFTER_STOP: Int = 10

  // Update count expectations
  val MIN_UPDATES_200MS: Int = 5
  val MAX_UPDATES_200MS: Int = 25

  // Timing calculations
  val TIMING_TOLERANCE: Int                 = 10
  def expectedUpdatesFor(timeMs: Long): Int = (timeMs / STANDARD_DELTA_TIME).toInt
  def minExpectedUpdates(timeMs: Long): Int = expectedUpdatesFor(timeMs) - TIMING_TOLERANCE
  def maxExpectedUpdates(timeMs: Long): Int = expectedUpdatesFor(timeMs) + TIMING_TOLERANCE

  // Health system test constants (only those not in GamePlayConstants)
  val TEST_ENTITY_MAX_HEALTH: Int                   = 100
  val TEST_ENTITY_HALF_HEALTH: Int                  = 50
  val TEST_ENTITY_LOW_HEALTH: Int                   = 20
  val TEST_ENTITY_VERY_LOW_HEALTH: Int              = 10
  val TEST_ENTITY_MINIMAL_HEALTH: Int               = 1
  val TEST_ENTITY_DEAD_HEALTH: Int                  = 0
  val TEST_DAMAGE_LIGHT: Int                        = 10
  val TEST_DAMAGE_MEDIUM: Int                       = 15
  val TEST_DAMAGE_HEAVY: Int                        = 20
  val TEST_DAMAGE_FATAL: Int                        = 30
  val TEST_HEALTH_QUARTER: Int                      = 25
  val TEST_HEALTH_THREE_QUARTER: Int                = 75
  val TEST_EXPECTED_HEALTH_AFTER_MEDIUM_DAMAGE: Int = 30
  val TEST_EXPECTED_HEALTH_AFTER_HEAVY_DAMAGE: Int  = 80
  val TEST_HEALTH_PERCENTAGE_QUARTER: Double        = 0.25
  val TEST_HEALTH_PERCENTAGE_FULL: Double           = 1.0
  val TEST_HEALTH_PERCENTAGE_ZERO: Double           = 0.0

  // Spawn system test constants
  val TEST_WIZARD_ROW: Int   = GRID_ROWS / 2
  val TEST_WIZARD_COL        = 2
  val TEST_SPAWN_COLUMN: Int = GRID_COLS - 1
  val TEST_WAVE_1            = 1
  val TEST_WAVE_2            = 2
  val TEST_MULTIPLE_UPDATES  = 5
  val TEST_MANY_UPDATES      = 10
  val TEST_SEED              = 42L

  // Elixir test constants
  val ELIXIR_WAIT_MARGIN: Long    = 500L
  val ELIXIR_SPEND_SMALL: Int     = 50
  val ELIXIR_SPEND_MEDIUM: Int    = 30
  val ELIXIR_SPEND_EXCESSIVE: Int = INITIAL_ELIXIR + 100
  val ELIXIR_ADD_AMOUNT: Int      = 75
  val ELIXIR_ADD_LARGE: Int       = 100
  val ELIXIR_SPEND_COMBINED: Int  = 250

  // Timer test constants
  val TEST_TIMER_ZERO: Long    = 0L
  val TEST_OLD_TIMESTAMP: Long = 123456L
  val TEST_TIME_BUFFER: Long   = 1000L
  val TEST_TIME_SHORT: Long    = 1000L
  val TEST_TIME_LONG: Long     = 10000L

  // Generator test constants
  val TEST_GENERATOR_ELIXIR_TINY: Int         = 5
  val TEST_GENERATOR_ELIXIR_LOW: Int          = 10
  val TEST_GENERATOR_ELIXIR_HIGH: Int         = 15
  val TEST_GENERATOR_COOLDOWN_SHORT: Long     = 3000L
  val TEST_GENERATOR_COOLDOWN_LONG: Long      = 5000L
  val TEST_GENERATOR_COOLDOWN_VERY_LONG: Long = 10000L

  // Test constants for movement
  val TEST_DELTA_TIME: Double   = 0.016
  val TEST_MIDDLE_ROW: Int      = GRID_ROWS / 2
  val TEST_MIDDLE_COL: Int      = GRID_COLS / 2
  val TEST_START_COL: Int       = GRID_COLS - 1
  val TEST_SPEED_SLOW           = 0.5
  val TEST_SPEED_NORMAL         = 1.0
  val TEST_SPEED_FAST           = 2.0
  val TEST_SPEED_ZERO           = 0.0
  val TEST_THROWER_STOP_COL     = 6
  val TEST_LEFT_BOUNDARY        = 0
  val TEST_RIGHT_BOUNDARY: Int  = GRID_COLS - 1
  val TEST_TOP_BOUNDARY         = 0
  val TEST_BOTTOM_BOUNDARY: Int = GRID_ROWS - 1

  // EntityFactory test constants
  val TEST_PROJECTILE_X: Double = 300.0
  val TEST_PROJECTILE_Y: Double = 200.0
  val TEST_WIZARD_X: Double     = 400.0
  val TEST_WIZARD_Y: Double     = 300.0
  val TEST_TROLL_X: Double      = 800.0
  val TEST_TROLL_Y: Double      = 400.0
  val TEST_CUSTOM_X: Double     = 500.0
  val TEST_CUSTOM_Y: Double     = 350.0

  // Image paths for projectiles
  val TEST_FIRE_PROJECTILE_PATH: String  = "/projectile/fire.png"
  val TEST_ICE_PROJECTILE_PATH: String   = "/projectile/ice.png"
  val TEST_TROLL_PROJECTILE_PATH: String = "/projectile/troll.png"
  val TEST_WIND_PROJECTILE_PATH: String  = "/projectile/base.png"

  // HealthBarRenderSystem test constants
  val TEST_HEALTH_BAR_X: Double           = 600.0
  val TEST_HEALTH_BAR_Y: Double           = 300.0
  val TEST_HEALTH_BAR_CUSTOM_X: Double    = 650.0
  val TEST_HEALTH_BAR_CUSTOM_Y: Double    = 350.0
  val TEST_HEALTH_BAR_MAX_HEALTH: Int     = 100
  val TEST_HEALTH_BAR_HALF_HEALTH: Int    = 50
  val TEST_HEALTH_BAR_HIGH_HEALTH: Int    = 80
  val TEST_HEALTH_BAR_MEDIUM_HEALTH: Int  = 50
  val TEST_HEALTH_BAR_LOW_HEALTH: Int     = 20
  val TEST_HEALTH_BAR_QUARTER_HEALTH: Int = 25
  val TEST_HEALTH_BAR_DAMAGE_MEDIUM: Int  = 20

  // Health bar thresholds (matching HealthBarComponent logic)
  val TEST_HEALTH_BAR_GREEN_THRESHOLD: Double    = 0.6
  val TEST_HEALTH_BAR_RED_THRESHOLD: Double      = 0.3
  val TEST_HEALTH_BAR_QUARTER_PERCENTAGE: Double = 0.25
  val TEST_HEALTH_BAR_TOLERANCE: Double          = 0.01

  // Health bar counts
  val TEST_NO_BARS: Int    = 0
  val TEST_ONE_BAR: Int    = 1
  val TEST_TWO_BARS: Int   = 2
  val TEST_THREE_BARS: Int = 3

  // Average health calculations
  val TEST_HEALTH_BAR_AVG_WIZARD_HEALTH: Double = 0.75 // (50 + 100) / 2 / 100
  val TEST_HEALTH_BAR_AVG_TROLL_HEALTH: Double  = 0.5  // (20 + 50 + 80) / 3 / 100
