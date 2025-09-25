package it.unibo.pps.wvt.utilities

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