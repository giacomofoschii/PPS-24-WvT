package it.unibo.pps.wvt.ecs.components

import it.unibo.pps.wvt.utilities.Position

sealed trait Component

case class PositionComponent(position: Position) extends Component
case class HealthComponent(currentHealth: Int, maxHealth: Int) extends Component {
  def isAlive: Boolean = currentHealth > 0
}
case class CostComponent(cost: Int) extends Component
case class ElixirGeneratorComponent(elixirPerSecond: Int, cooldown: Long) extends Component
case class SpriteComponent(spritePath: String) extends Component

case class AttackComponent(damage: Int, range: Double, cooldown: Long) extends Component
case class CooldownComponent(remainingTime: Long) extends Component
case class MovementComponent(speed: Double) extends Component

sealed trait EntityTypeComponent extends Component
case class WizardTypeComponent(wizardType: WizardType) extends EntityTypeComponent
case class TrollTypeComponent(trollType: TrollType) extends EntityTypeComponent

enum WizardType {
  case Generator, Wind, Barrier, Fire, Ice
}

enum TrollType {
  case Base, Warrior, Assassin, Thrower
}