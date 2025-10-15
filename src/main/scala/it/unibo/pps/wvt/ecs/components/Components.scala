package it.unibo.pps.wvt.ecs.components

import it.unibo.pps.wvt.ecs.components.MovementComponent.*
import it.unibo.pps.wvt.ecs.core.EntityId
import it.unibo.pps.wvt.utilities.Position
import scalafx.scene.paint.Color

sealed trait Component

// Movement components
case class PositionComponent(position: Position) extends Component
case class MovementComponent(speed: Double)      extends Component
case class ZigZagStateComponent(
    spawnRow: Int,
    currentPhase: ZigZagPhase,
    phaseStartTime: Long,
    alternateRow: Int
) extends Component

enum ZigZagPhase:
  case OnSpawnRow, OnAlternateRow

// Health components
case class HealthComponent(currentHealth: Int, maxHealth: Int) extends Component:
  def isAlive: Boolean = currentHealth > 0

// Elixir components
case class CostComponent(cost: Int)                                       extends Component
case class ElixirGeneratorComponent(elixirPerSecond: Int, cooldown: Long) extends Component

// Combat components
case class AttackComponent(damage: Int, range: Double, cooldown: Long)  extends Component
case class DamageComponent(amount: Int, projectileType: ProjectileType) extends Component
case class CollisionComponent(amount: Int)                              extends Component
case class CooldownComponent(remainingTime: Long)                       extends Component
case class BlockedComponent(blockedBy: EntityId)                        extends Component
case class FreezedComponent(remainingTime: Long, speedModifier: Double) extends Component

// UI components
case class ImageComponent(imagePath: String) extends Component
case class HealthBarComponent(
    visible: Boolean = true,
    barColor: Color = Color.Green,
    barWidth: Double = 40.0,
    barHeight: Double = 4.0,
    offsetY: Double = -10.0
) extends Component:

  def updateColorByHealthPercentage(percentage: Double): HealthBarComponent =
    val newColor = percentage match
      case p if p > 0.6 => Color.Green
      case p if p > 0.3 => Color.Yellow
      case _            => Color.Red
    copy(barColor = newColor)

  def withVisibility(isVisible: Boolean): HealthBarComponent =
    copy(visible = isVisible)

// Entities types components
sealed trait EntityTypeComponent                                   extends Component
case class WizardTypeComponent(wizardType: WizardType)             extends EntityTypeComponent
case class TrollTypeComponent(trollType: TrollType)                extends EntityTypeComponent
case class ProjectileTypeComponent(projectileType: ProjectileType) extends EntityTypeComponent

enum WizardType:
  case Generator, Wind, Barrier, Ice, Fire

enum TrollType:
  case Base, Warrior, Assassin, Thrower

enum ProjectileType:
  case Wind, Fire, Ice, Troll
