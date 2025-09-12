package it.unibo.pps.wvt.model

// Base Wizard trait
trait Wizard extends Entity {
  def cost: Int
  def range: Int
  def generateElixir: Int = 0
  override def entityType: EntityType.WizardType

  // Common damage implementation
  protected def takeDamageImpl(damage: Int, copyFn: Int => Wizard): Wizard =
    copyFn(math.max(0, health - damage))
}

// Generator Wizard - Generates elixir, no attack
case class GeneratorWizard(
                            id: String,
                            position: Position,
                            health: Int,
                            maxHealth: Int = 50
                          ) extends Wizard {
  val cost = 50
  val range = 0

  override def entityType = EntityType.Generator
  override def generateElixir: Int = 25

  def takeDamage(damage: Int): GeneratorWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[GeneratorWizard]
}

// Wind Wizard - Basic attacker
case class WindWizard(
                       id: String,
                       position: Position,
                       health: Int,
                       maxHealth: Int = 100
                     ) extends Wizard with Attacker {
  val cost = 100
  val attackDamage = 25
  val range = 3
  val canAttack = true
  val projectileType = ProjectileType.Wind

  override def entityType = EntityType.Wind

  def takeDamage(damage: Int): WindWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[WindWizard]
}

// Barrier Wizard - High health defender, no attack
case class BarrierWizard(
                          id: String,
                          position: Position,
                          health: Int,
                          maxHealth: Int = 300
                        ) extends Wizard {
  val cost = 50
  val range = 0

  override def entityType = EntityType.Barrier

  def takeDamage(damage: Int): BarrierWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[BarrierWizard]
}

// Fire Wizard - High damage attacker
case class FireWizard(
                       id: String,
                       position: Position,
                       health: Int,
                       maxHealth: Int = 100
                     ) extends Wizard with Attacker {
  val cost = 150
  val attackDamage = 50
  val range = 4
  val canAttack = true
  val projectileType = ProjectileType.Fire

  override def entityType = EntityType.Fire

  def takeDamage(damage: Int): FireWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[FireWizard]
}

// Ice Wizard - Slowing attacker
case class IceWizard(
                      id: String,
                      position: Position,
                      health: Int,
                      maxHealth: Int = 100
                    ) extends Wizard with Attacker {
  val cost = 175
  val attackDamage = 15
  val range = 3
  val canAttack = true
  val projectileType = ProjectileType.Ice

  override def entityType = EntityType.Ice

  def takeDamage(damage: Int): IceWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[IceWizard]
}