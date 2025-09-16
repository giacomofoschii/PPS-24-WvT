package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.model.Position

object TestInputConstants:
  
  val GRID_OFFSET_WIDTH: Double = GRID_OFFSET_X
  val GRID_OFFSET_HEIGHT: Double = GRID_OFFSET_Y
  // Grid boundaries
  val GRID_START_X: Int = GRID_OFFSET_X.toInt
  val GRID_START_Y: Int = GRID_OFFSET_Y.toInt
  val GRID_END_X: Int = GRID_OFFSET_X.toInt + (GRID_COLS * CELL_WIDTH)
  val GRID_END_Y: Int = GRID_OFFSET_Y.toInt + (GRID_ROWS * CELL_HEIGHT)

  // Valid coordinates inside grid
  val INSIDE_GRID_X: Int = GRID_START_X + 10
  val INSIDE_GRID_Y: Int = GRID_START_Y + 10

  // Invalid coordinates outside grid
  val OUTSIDE_GRID_X: Int = 10
  val OUTSIDE_GRID_Y: Int = 10
  val NEGATIVE_X: Int = -5
  val NEGATIVE_Y: Int = -5
  val TOO_FAR_X: Int = GRID_END_X + 10
  val TOO_FAR_Y: Int = GRID_END_Y + 10

  // Edge case coordinates
  val LAST_VALID_X: Int = GRID_END_X - 1
  val LAST_VALID_Y: Int = GRID_END_Y - 1
  val FIRST_INVALID_X: Int = GRID_END_X
  val FIRST_INVALID_Y: Int = GRID_END_Y
  val BEFORE_BOUNDARY_X: Int = GRID_START_X - 1
  val BEFORE_BOUNDARY_Y: Int = GRID_START_Y - 1

  // Cell center coordinates
  val FIRST_CELL_CENTER_X: Int = GRID_START_X + CELL_WIDTH / 2
  val FIRST_CELL_CENTER_Y: Int = GRID_START_Y + CELL_HEIGHT / 2
  val SECOND_CELL_CENTER_X: Int = GRID_START_X + CELL_WIDTH + CELL_WIDTH / 2
  val SECOND_CELL_CENTER_Y: Int = GRID_START_Y + CELL_HEIGHT + CELL_HEIGHT / 2

  // Specific cell coordinates
  def cellCenterX(col: Int): Int = GRID_START_X + col * CELL_WIDTH + CELL_WIDTH / 2
  def cellCenterY(row: Int): Int = GRID_START_Y + row * CELL_HEIGHT + CELL_HEIGHT / 2
  def cellCornerX(col: Int): Int = GRID_START_X + col * CELL_WIDTH
  def cellCornerY(row: Int): Int = GRID_START_Y + row * CELL_HEIGHT

  // Corner coordinates
  val TOP_LEFT_X: Int = GRID_START_X + 1
  val TOP_LEFT_Y: Int = GRID_START_Y + 1
  val TOP_RIGHT_X: Int = GRID_START_X + ((GRID_COLS - 1) * CELL_WIDTH) + 10
  val TOP_RIGHT_Y: Int = GRID_START_Y + 10
  val BOTTOM_LEFT_X: Int = GRID_START_X + 10
  val BOTTOM_LEFT_Y: Int = GRID_START_Y + ((GRID_ROWS - 1) * CELL_HEIGHT) + 10
  val BOTTOM_RIGHT_X: Int = GRID_START_X + ((GRID_COLS - 1) * CELL_WIDTH) + 10
  val BOTTOM_RIGHT_Y: Int = GRID_START_Y + ((GRID_ROWS - 1) * CELL_HEIGHT) + 10

  // Valid positions
  val TOP_LEFT_POS: Position = Position(0, 0)
  val TOP_RIGHT_POS: Position = Position(0, GRID_COLS - 1)
  val BOTTOM_LEFT_POS: Position = Position(GRID_ROWS - 1, 0)
  val BOTTOM_RIGHT_POS: Position = Position(GRID_ROWS - 1, GRID_COLS - 1)
  val CENTER_POS: Position = Position(GRID_ROWS / 2, GRID_COLS / 2)
  val SAMPLE_POS_1: Position = Position(1, 2)
  val SAMPLE_POS_2: Position = Position(2, 3)
  val SAMPLE_POS_3: Position = Position(3, 7)

  // Invalid position
  val INVALID_POS: Position = Position(-1, -1, allowInvalid = true)

  // Expected screen coordinates for positions
  val EXPECTED_TOP_LEFT_SCREEN_X: Double = GRID_OFFSET_X + CELL_WIDTH / 2
  val EXPECTED_TOP_LEFT_SCREEN_Y: Double = GRID_OFFSET_Y + CELL_HEIGHT / 2
  val EXPECTED_SAMPLE_POS_2_SCREEN_X: Double = GRID_OFFSET_X + 3 * CELL_WIDTH + CELL_WIDTH / 2
  val EXPECTED_SAMPLE_POS_2_SCREEN_Y: Double = GRID_OFFSET_Y + 2 * CELL_HEIGHT + CELL_HEIGHT / 2

  // Test positions list
  val TEST_POSITIONS: List[Position] = List(
    TOP_LEFT_POS,
    SAMPLE_POS_2,
    BOTTOM_RIGHT_POS,
    SAMPLE_POS_3
  )

  // Test click coordinates
  val TEST_CLICKS: List[(Int, Int)] = List(
    (GRID_START_X + 50, GRID_START_Y + 50),
    (GRID_START_X + 250, GRID_START_Y + 150),
    (GRID_START_X + 450, GRID_START_Y + 350)
  )

  // Boundary test coordinates  
  val BOUNDARY_COORDS: List[(Int, Int)] = List(
    (GRID_START_X, GRID_START_Y),
    (GRID_START_X + 100, GRID_START_Y + 100),
    (LAST_VALID_X, LAST_VALID_Y)
  )

  // Invalid test coordinates
  val INVALID_COORDS: List[(Int, Int)] = List(
    (OUTSIDE_GRID_X, OUTSIDE_GRID_Y),
    (NEGATIVE_X, NEGATIVE_Y),
    (TOO_FAR_X, TOO_FAR_Y),
    (FIRST_INVALID_X, FIRST_INVALID_Y)
  )