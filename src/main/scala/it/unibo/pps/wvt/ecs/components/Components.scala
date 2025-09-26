package it.unibo.pps.wvt.ecs.components

import it.unibo.pps.wvt.ecs.core.EntityId
import it.unibo.pps.wvt.utilities.Position

sealed trait Component

// Movement components
case class PositionComponent(position: Position) extends Component
case class MovementComponent(speed: Double) extends Component

// Health components
case class HealthComponent(currentHealth: Int, maxHealth: Int) extends Component {
  def isAlive: Boolean = currentHealth > 0
}

// Elixir components
case class CostComponent(cost: Int) extends Component
case class ElixirGeneratorComponent(elixirPerSecond: Int, cooldown: Long) extends Component

// Combat components
case class AttackComponent(damage: Int, range: Double, cooldown: Long) extends Component
case class DamageComponent(amount: Int, projectileType: ProjectileType) extends Component
case class CollisionComponent(amount: Int) extends Component
case class CooldownComponent(remainingTime: Long) extends Component

// UI components
case class SpriteComponent(spritePath: String) extends Component

// Entities types components
sealed trait EntityTypeComponent extends Component
case class WizardTypeComponent(wizardType: WizardType) extends EntityTypeComponent
case class TrollTypeComponent(trollType: TrollType) extends EntityTypeComponent
case class ProjectileTypeComponent(projectileType: ProjectileType) extends EntityTypeComponent

enum WizardType {
  case Generator, Wind, Barrier, Fire, Ice
}

enum TrollType {
  case Base, Warrior, Assassin, Thrower
}

enum ProjectileType {
  case Base, Fire, Ice, Troll
}