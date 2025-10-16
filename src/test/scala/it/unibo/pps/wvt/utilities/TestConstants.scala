package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.ecs.config.WaveLevel

object TestConstants:

  // Grid positions
  val GRID_ROW_START: Int = 0
  val GRID_ROW_MID: Int   = 2
  val GRID_ROW_END: Int   = 4
  val GRID_COL_START: Int = 0
  val GRID_COL_MID: Int   = 3
  val GRID_COL_END: Int   = 6
  // Added for specific test cases
  val GRID_ROW_NEAR_END: Int = 4
  val GRID_COL_NEAR_MID: Int = 2
  val GRID_COL_NEAR_END: Int = 4
  val GRID_COLS_LOGICAL: Int = 7 // Assuming 7 columns in the logical grid

  // Position values
  val POS_X_START: Float = 0f
  val POS_X_MID: Float   = 400f
  val POS_X_END: Float   = 800f
  val POS_Y_START: Float = 0f
  val POS_Y_MID: Float   = 300f
  val POS_Y_END: Float   = 600f

  // Health values
  val HEALTH_ZERO: Int = 0
  val HEALTH_LOW: Int  = 25
  val HEALTH_MID: Int  = 50
  val HEALTH_FULL: Int = 100
  val HEALTH_HIGH: Int = 150

  // Elixir values
  val ELIXIR_ZERO: Int         = 0
  val ELIXIR_LOW: Int          = 50
  val ELIXIR_SPEND_AMOUNT: Int = 50 // New constant for testing elixir spending
  val ELIXIR_START: Int        = 100
  val ELIXIR_MID: Int          = 200
  val ELIXIR_HIGH: Int         = 500

  // Wave numbers
  val WAVE_FIRST: Int  = 1
  val WAVE_SECOND: Int = 2 // New constant for testing wave progression
  val WAVE_MID: Int    = 5
  val WAVE_HIGH: Int   = 10
  val WAVE_MAX: Int    = 20

  // Speed values
  val SPEED_ZERO: Float      = 0f
  val SPEED_SLOW: Float      = 1f
  val SPEED_NORMAL: Float    = 3f
  val SPEED_FAST: Float      = 5f
  val SPEED_VERY_FAST: Float = 10f

  // Damage values
  val DAMAGE_ZERO: Int      = 0
  val DAMAGE_LOW: Int       = 10
  val DAMAGE_NORMAL: Int    = 20
  val DAMAGE_HIGH: Int      = 50
  val DAMAGE_VERY_HIGH: Int = 100

  // Time values (seconds)
  val DELTA_TIME_FRAME: Float    = 0.016f // ~60 FPS
  val DELTA_TIME_HALF_SEC: Float = 0.5f
  val DELTA_TIME_ONE_SEC: Float  = 1.0f
  val DELTA_TIME_TWO_SEC: Float  = 2.0f

  // Cooldown values
  val COOLDOWN_FAST: Float   = 0.5f
  val COOLDOWN_NORMAL: Float = 1.0f
  val COOLDOWN_SLOW: Float   = 2.0f

  // Range values
  val RANGE_MELEE: Float  = 1.5f
  val RANGE_SHORT: Float  = 3.0f
  val RANGE_MEDIUM: Float = 5.0f
  val RANGE_LONG: Float   = 10.0f

  // Test thresholds
  val EPSILON: Float            = 0.001f
  val POSITION_TOLERANCE: Float = 0.1f

  // Game dimensions
  val GAME_WIDTH: Int  = 800
  val GAME_HEIGHT: Int = 600

  // Movement and timing
  val DELTA_TIME_MS      = 16.0
  val ZIGZAG_DURATION_MS = 5000L
  val MOVEMENT_TOLERANCE = 5.0

  // Spawn timing and wave settings
  val SPAWN_INTERVAL_SHORT: Float  = 1.0f
  val SPAWN_INTERVAL_NORMAL: Float = 2.0f
  val SPAWN_INTERVAL_LONG: Float   = 5.0f
  val SPAWN_SEED                   = 42L
  val INITIAL_SPAWN_DELAY_MS       = 3000L
  val BATCH_DELAY_MS               = 500L
  val SHORT_WAIT_MS                = 50L
  val LONG_WAIT_MS                 = 3000L
  val SPAWN_ATTEMPTS: Int          = 10
  val WAVE_ONE_MAX_TROLLS: Int     = WaveLevel.maxTrollsPerWave(WAVE_FIRST)

  // Entity counts
  val ENTITY_COUNT_SINGLE: Int = 1
  val ENTITY_COUNT_FEW: Int    = 3
  val ENTITY_COUNT_MANY: Int   = 10

  // Update counts for tests
  val UPDATES_COUNT_SHORT: Int  = 10
  val UPDATES_COUNT_MEDIUM: Int = 50
  val UPDATES_COUNT_LONG: Int   = 100
  val UPDATES_PER_SECOND: Int   = 60 // Approx. number of updates to simulate 1 second

  // Delay values for tests in milliseconds
  val DELAY_FRAME_MS: Long  = 17 // ~60 FPS
  val DELAY_SHORT_MS: Long  = 30
  val DELAY_MEDIUM_MS: Long = 50

  // Elixir generation
  val ELIXIR_GEN_RATE: Float = 1.0f
  val ELIXIR_GEN_AMOUNT: Int = 5

  // Game engine timing constants
  val SHORT_SLEEP_MS          = 100L
  val MEDIUM_SLEEP_MS         = 200L
  val LONG_SLEEP_MS           = 1000L
  val ENGINE_STARTUP_DELAY_MS = 50L
  val TIME_INCREMENT          = 16L

  // Game loop timing and FPS constants
  val LOOP_STARTUP_DELAY_MS    = 50L
  val FPS_MEASUREMENT_DELAY_MS = 1100L
  val SHORT_RUN_MS             = 200L
  val MEDIUM_RUN_MS            = 500L
  val EXPECTED_FPS_MIN         = 30
  val EXPECTED_FPS_MAX         = 70
