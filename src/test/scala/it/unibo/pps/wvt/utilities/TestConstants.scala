package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.GamePlayConstants.*

object TestConstants:
  // Time constants (in milliseconds)
  val SHORT_DELAY: Long = 50L
  val MEDIUM_DELAY: Long = 100L
  val LONG_DELAY: Long = 200L
  val FPS_CALCULATION_DELAY: Long = 1100L
  val STANDARD_DELTA_TIME: Long = 16L // ~60 FPS
  val EXPECTED_FRAME_TIME: Long = 500L

  // Loop iterations
  val SMALL_ITERATION_COUNT: Int = 3
  val MEDIUM_ITERATION_COUNT: Int = 10

  // Expected values
  val INITIAL_TIME: Long = 0L
  val INITIAL_ENTITY_COUNT: Int = 0
  val INITIAL_FPS: Int = 0

  // FPS testing
  val MIN_EXPECTED_FPS: Int = 45
  val MAX_EXPECTED_FPS: Int = 75
  val MAX_UPDATES_AFTER_STOP: Int = 10

  // Update count expectations
  val MIN_UPDATES_200MS: Int = 5
  val MAX_UPDATES_200MS: Int = 25

  // Timing calculations
  val TIMING_TOLERANCE: Int = 10
  def expectedUpdatesFor(timeMs: Long): Int = (timeMs / STANDARD_DELTA_TIME).toInt
  def minExpectedUpdates(timeMs: Long): Int = expectedUpdatesFor(timeMs) - TIMING_TOLERANCE
  def maxExpectedUpdates(timeMs: Long): Int = expectedUpdatesFor(timeMs) + TIMING_TOLERANCE

  // Health system test constants (only those not in GamePlayConstants)
  val TEST_ENTITY_MAX_HEALTH: Int = 100
  val TEST_ENTITY_HALF_HEALTH: Int = 50
  val TEST_ENTITY_LOW_HEALTH: Int = 20
  val TEST_ENTITY_VERY_LOW_HEALTH: Int = 10
  val TEST_ENTITY_MINIMAL_HEALTH: Int = 1
  val TEST_ENTITY_DEAD_HEALTH: Int = 0
  val TEST_DAMAGE_LIGHT: Int = 10
  val TEST_DAMAGE_MEDIUM: Int = 15
  val TEST_DAMAGE_HEAVY: Int = 20
  val TEST_DAMAGE_FATAL: Int = 30
  val TEST_HEALTH_QUARTER: Int = 25
  val TEST_HEALTH_THREE_QUARTER: Int = 75
  val TEST_EXPECTED_HEALTH_AFTER_MEDIUM_DAMAGE: Int = 30
  val TEST_EXPECTED_HEALTH_AFTER_HEAVY_DAMAGE: Int = 80
  val TEST_HEALTH_PERCENTAGE_QUARTER: Double = 0.25
  val TEST_HEALTH_PERCENTAGE_FULL: Double = 1.0
  val TEST_HEALTH_PERCENTAGE_ZERO: Double = 0.0

  // Spawn system test constants
  val SPAWN_INTERVAL_SHORT: Long = 100L
  val SPAWN_INTERVAL_MEDIUM: Long = 500L
  val SPAWN_INTERVAL_LONG: Long = 1000L
  val SPAWN_INTERVAL_FACTORY: Long = 3000L
  val SLEEP_AFTER_SPAWN: Long = 150L
  val SLEEP_BEFORE_UPDATE: Long = 200L
  val SLEEP_BETWEEN_BATCHES: Long = 60L
  val SLEEP_AFTER_INTERVAL: Long = 200L
  val MAX_ATTEMPTS: Int = 20
  val RANDOM_SEED: Int = 42
  val FACTORY_SEED: Long = 12345L
  val MULTI_BATCH_COUNT: Int = 10

  // Elixir test constants
  val TEST_ELIXIR_AMOUNT: Int = 100
  val TEST_ELIXIR_SPEND: Int = 50
  val TEST_ELIXIR_REMAINING: Int = TEST_ELIXIR_AMOUNT - TEST_ELIXIR_SPEND // 50
  val TEST_ELIXIR_INSUFFICIENT: Int = 30
  val TEST_ELIXIR_AFTER_ADD: Int = TEST_ELIXIR_AMOUNT + TEST_ELIXIR_SPEND // 150
  val TEST_ELIXIR_TOO_MUCH: Int = TEST_ELIXIR_AMOUNT + 1 // 101
  val TEST_ELIXIR_HIGH: Int = 500

  // Timer test constants
  val TEST_TIMER_ZERO: Long = 0L
  val TEST_OLD_TIMESTAMP: Long = 123456L
  val TEST_TIME_BUFFER: Long = 1000L
  val TEST_TIME_SHORT: Long = 1000L
  val TEST_TIME_LONG: Long = 10000L

  // Generator test constants
  val TEST_GENERATOR_ELIXIR_TINY: Int = 5
  val TEST_GENERATOR_ELIXIR_LOW: Int = 10
  val TEST_GENERATOR_ELIXIR_HIGH: Int = 15
  val TEST_GENERATOR_COOLDOWN_SHORT: Long = 3000L
  val TEST_GENERATOR_COOLDOWN_LONG: Long = 5000L
  val TEST_GENERATOR_COOLDOWN_VERY_LONG: Long = 10000L

  // Calculated test constants
  val TEST_ELIXIR_MULTIPLE_GENERATORS: Int = TEST_ELIXIR_AMOUNT + TEST_GENERATOR_ELIXIR_LOW + TEST_GENERATOR_ELIXIR_HIGH
  val TEST_ELIXIR_FULL_CYCLE: Int = TEST_ELIXIR_AMOUNT + PERIODIC_ELIXIR + TEST_GENERATOR_ELIXIR_TINY