package it.unibo.pps.wvt.utilities

object TestConstants {
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
}