package it.unibo.pps.wvt.model

import it.unibo.pps.wvt.utilities.GamePlayConstants._

// Base Troll trait
trait Troll extends Entity with Attacker with Moveable {
  val canAttack = true
  val projectileType: ProjectileType = ProjectileType.TrollAttack
  override def entityType: EntityType.TrollType

  // Common damage implementation  
  protected def takeDamageImpl(damage: Int, copyFn: Int => Troll): Troll =
    copyFn(math.max(0, health - damage))
}

// Base Troll - Standard balanced troll
case class BaseTroll(
                      id: String,
                      position: Position,
                      health: Int,
                      maxHealth: Int = BASE_TROLL_HEALTH,
                      speed: Int = BASE_TROLL_SPEED
                    ) extends Troll {
  val attackDamage: Int = BASE_TROLL_DAMAGE

  override def entityType: EntityType.TrollType = EntityType.Base

  def takeDamage(damage: Int): BaseTroll =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[BaseTroll]

  def updatePosition(newPosition: Position): BaseTroll = copy(position = newPosition)
}

// Warrior Troll - High health, low speed, medium damage
case class WarriorTroll(
                         id: String,
                         position: Position,
                         health: Int,
                         maxHealth: Int = WARRIOR_TROLL_HEALTH,
                         speed: Int = WARRIOR_TROLL_SPEED
                       ) extends Troll {
  val attackDamage: Int = WARRIOR_TROLL_DAMAGE

  override def entityType: EntityType.TrollType = EntityType.Warrior

  def takeDamage(damage: Int): WarriorTroll =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[WarriorTroll]

  def updatePosition(newPosition: Position): WarriorTroll = copy(position = newPosition)
}

// Assassin Troll - Very low health, very high speed, high damage
case class AssassinTroll(
                          id: String,
                          position: Position,
                          health: Int,
                          maxHealth: Int = ASSASSIN_TROLL_HEALTH,
                          speed: Int = ASSASSIN_TROLL_SPEED
                        ) extends Troll {
  val attackDamage: Int = ASSASSIN_TROLL_DAMAGE

  override def entityType: EntityType.TrollType = EntityType.Assassin

  def takeDamage(damage: Int): AssassinTroll =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[AssassinTroll]

  def updatePosition(newPosition: Position): AssassinTroll = copy(position = newPosition)
}

// Thrower Troll - Stays at back, long range attacks
case class ThrowerTroll(
                         id: String,
                         position: Position,
                         health: Int,
                         maxHealth: Int = THROWER_TROLL_HEALTH,
                         speed: Int = THROWER_TROLL_SPEED
                       ) extends Troll {
  val attackDamage = THROWER_TROLL_DAMAGE

  override def entityType: EntityType.TrollType = EntityType.Thrower

  def takeDamage(damage: Int): ThrowerTroll =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[ThrowerTroll]

  // Thrower doesn't move
  def updatePosition(newPosition: Position): ThrowerTroll = this
}