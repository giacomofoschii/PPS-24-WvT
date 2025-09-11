package it.unibo.pps.wvt.model

case class Position private (row: Int, col: Int)

object Position {
  def apply(row: Int, col: Int): Either[String, Position] =
    if (row < 0 || row >= 5) Left(s"Row must be between 0-4, got $row")
    else if (col < 0 || col >= 9) Left(s"Column must be between 0-8, got $col")
    else Right(Position(row, col))
}

trait Entity {
  def id: String
  def position: Position
  def health: Int
  def maxHealth: Int
  def isAlive: Boolean = health > 0
  def takeDamage(damage: Int): Entity
  def entityType: EntityType
}