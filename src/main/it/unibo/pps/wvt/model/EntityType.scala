package it.unibo.wvt.models

sealed trait EntityType
object EntityType {
  sealed trait WizardType extends EntityType
  sealed trait TrollType extends EntityType

  case object Generator extends WizardType
  case object Wind extends WizardType
  case object Barrier extends WizardType
  case object Fire extends WizardType
  case object Ice extends WizardType

  case object Base extends TrollType
  case object Warrior extends TrollType
  case object Assassin extends TrollType
  case object Thrower extends TrollType
}

case class Projectile(
                       damage: Int,
                       speed: Int,
                       source: Position,
                       target: Position,
                       projectileType: ProjectileType
                     )

sealed trait ProjectileType
object ProjectileType {
  case object Wind extends ProjectileType
  case object Fire extends ProjectileType
  case object Ice extends ProjectileType
  case object TrollAttack extends ProjectileType
}