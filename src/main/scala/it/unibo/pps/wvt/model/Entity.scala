package it.unibo.pps.wvt.model

import it.unibo.pps.wvt.utilities.ViewConstants._
import it.unibo.pps.wvt.utilities.GamePlayConstants._

case class Position(row: Int, col: Int, allowInvalid: Boolean = false):
  if !allowInvalid then
    require(row >= 0 && row < GRID_ROWS, s"Row must be between 0 and ${GRID_ROWS - 1}")
    require(col >= 0 && col < GRID_COLS, s"Col must be between 0 and ${GRID_COLS - 1}")

  def isValid: Boolean = row >= 0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS

trait Entity {
  def id: String
  def position: Position
  def health: Int
  def maxHealth: Int
  def isAlive: Boolean = health > 0
  def takeDamage(damage: Int): Entity
  def entityType: EntityType
}

trait Attacker { this: Entity =>
  def attackDamage: Int
  def canAttack: Boolean
  def projectileType: ProjectileType

  def attack(): Option[Projectile] =
    if (canAttack)
      Some(Projectile(
        id = s"${entityType.toString.toLowerCase}-proj-${System.currentTimeMillis()}",
        damage = attackDamage,
        speed = PROJECTILE_SPEED,
        source = position,
        projectileType = projectileType
      ))
    else None
}

trait Moveable { this: Entity =>
  def speed: Int
  def updatePosition(newPosition: Position): Entity
}