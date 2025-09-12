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

trait Attacker { this: Entity =>
  def attackDamage: Int
  def canAttack: Boolean
  def projectileType: ProjectileType

  def attack(): Option[Projectile] = {
    if (canAttack) {
      Some(Projectile(
        id = s"${entityType.toString.toLowerCase}-proj-${System.currentTimeMillis()}",
        damage = attackDamage,
        speed = 5,
        source = position,
        projectileType = projectileType
      ))
    } else None
  }
}

trait Moveable { this: Entity =>
  def speed: Int
  def updatePosition(newPosition: Position): Entity
}