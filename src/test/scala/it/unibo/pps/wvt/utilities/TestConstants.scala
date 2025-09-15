package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.model.Position

object TestConstants {
  
  // Game engine constants
  val INITIAL_WAVE_NUM = 1
  val FIRST_POS: Position = Position(2, 5)
  val SECOND_POS: Position = Position(2, 2)
  val DAMAGE = 50
  val DAMAGE_AND_DESTROY = 150
  val POOR_ELIXIR = 50
  val DELTA = 100L
  val POS: Position = Position(2, 5)
  
  // Game loop constants
  val TEST_FPS_LOW = 30
  val TEST_FPS_VERY_LOW = 10
  val TEST_FPS_MEDIUM = 45
  val SMALL_DELAY = 100
  val MEDIUM_DELAY = 200
  val TEST_DURATION_HALF_SEC = 550
}