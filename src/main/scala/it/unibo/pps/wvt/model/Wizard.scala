package it.unibo.pps.wvt.model

import it.unibo.pps.wvt.utilities.GamePlayConstants._

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
                            maxHealth: Int = GENERATOR_WIZARD_HEALTH
                          ) extends Wizard {
  val cost = GENERATOR_WIZARD_COST
  val range = GENERATOR_WIZARD_RANGE

  override def entityType = EntityType.Generator
  override def generateElixir: Int = GENERATOR_WIZARD_ELIXIR

  def takeDamage(damage: Int): GeneratorWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[GeneratorWizard]
}

// Wind Wizard - Basic attacker
case class WindWizard(
                       id: String,
                       position: Position,
                       health: Int,
                       maxHealth: Int = WIND_WIZARD_HEALTH
                     ) extends Wizard with Attacker {
  val cost = WIND_WIZARD_COST
  val attackDamage: Int = WIND_WIZARD_ATTACK_DAMAGE
  val range = WIND_WIZARD_RANGE
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
                          maxHealth: Int = BARRIER_WIZARD_HEALTH
                        ) extends Wizard {
  val cost = BARRIER_WIZARD_COST
  val range = BARRIER_WIZARD_RANGE

  override def entityType = EntityType.Barrier

  def takeDamage(damage: Int): BarrierWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[BarrierWizard]
}

// Fire Wizard - High damage attacker
case class FireWizard(
                       id: String,
                       position: Position,
                       health: Int,
                       maxHealth: Int = FIRE_WIZARD_HEALTH
                     ) extends Wizard with Attacker {
  val cost = FIRE_WIZARD_COST
  val attackDamage = FIRE_WIZARD_ATTACK_DAMAGE
  val range = FIRE_WIZARD_RANGE
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
                      maxHealth: Int = ICE_WIZARD_HEALTH
                    ) extends Wizard with Attacker {
  val cost = ICE_WIZARD_COST
  val attackDamage = ICE_WIZARD_ATTACK_DAMAGE
  val range = ICE_WIZARD_RANGE
  val canAttack = true
  val projectileType = ProjectileType.Ice

  override def entityType = EntityType.Ice

  def takeDamage(damage: Int): IceWizard =
    takeDamageImpl(damage, newHealth => copy(health = newHealth)).asInstanceOf[IceWizard]
}