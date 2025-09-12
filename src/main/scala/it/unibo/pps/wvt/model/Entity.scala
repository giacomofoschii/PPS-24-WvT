package it.unibo.pps.wvt.model

case class Position(row: Int, col: Int) {
  require(row >= 0 && row < 5, "Row must be between 0 and 4")
  require(col >= 0 && col < 9, "Column must be between 0 and 8")
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