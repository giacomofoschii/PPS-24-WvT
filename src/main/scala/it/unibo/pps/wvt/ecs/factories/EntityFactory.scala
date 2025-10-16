package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.scene.paint.Color

case class ProjectileConfig(
    projectileType: ProjectileType,
    damage: Int,
    imagePath: String
)

case class WizardConfig(
    wizardType: WizardType,
    health: Int,
    cost: Int,
    imagePath: String,
    attack: Option[AttackComponent] = None,
    elixirGenerator: Option[ElixirGeneratorComponent] = None
)

case class TrollConfig(
    trollType: TrollType,
    health: Int,
    speed: Double,
    damage: Int,
    range: Double,
    cooldown: Long,
    imagePath: String
)

trait EntityBuilder[T]:
  def buildComponents(config: T, position: Position): List[Component]

object EntityBuilder:
  given EntityBuilder[ProjectileConfig] with
    def buildComponents(config: ProjectileConfig, position: Position): List[Component] =
      List(
        PositionComponent(position),
        MovementComponent(PROJECTILE_SPEED),
        ProjectileTypeComponent(config.projectileType),
        DamageComponent(config.damage, config.projectileType),
        ImageComponent(config.imagePath)
      )

  given EntityBuilder[WizardConfig] with
    def buildComponents(config: WizardConfig, position: Position): List[Component] =
      val baseComponents = List(
        PositionComponent(position),
        HealthComponent(config.health, config.health),
        CostComponent(config.cost),
        ImageComponent(config.imagePath),
        WizardTypeComponent(config.wizardType),
        HealthBarComponent(
          barColor = Color.Blue,
          barWidth = HEALTH_BAR_WIDTH,
          offsetY = HEALTH_BAR_OFFSET_Y
        )
      )
      val optionalComponents = List(config.attack, config.elixirGenerator).flatten
      baseComponents ++ optionalComponents

  given EntityBuilder[TrollConfig] with
    def buildComponents(config: TrollConfig, position: Position): List[Component] =
      List(
        PositionComponent(position),
        TrollTypeComponent(config.trollType),
        HealthComponent(config.health, config.health),
        MovementComponent(config.speed),
        AttackComponent(config.damage, config.range, config.cooldown),
        ImageComponent(config.imagePath),
        HealthBarComponent(
          barColor = Color.Red,
          barWidth = HEALTH_BAR_WIDTH,
          offsetY = HEALTH_BAR_OFFSET_Y
        )
      )

object EntityFactory:

  private val projectileConfigs: Map[ProjectileType, ProjectileConfig] = Map(
    ProjectileType.Fire -> ProjectileConfig(
      ProjectileType.Fire,
      FIRE_WIZARD_ATTACK_DAMAGE,
      "/projectile/fire.png"
    ),
    ProjectileType.Ice -> ProjectileConfig(
      ProjectileType.Ice,
      ICE_WIZARD_ATTACK_DAMAGE,
      "/projectile/ice.png"
    ),
    ProjectileType.Troll -> ProjectileConfig(
      ProjectileType.Troll,
      THROWER_TROLL_DAMAGE,
      "/projectile/troll.png"
    ),
    ProjectileType.Wind -> ProjectileConfig(
      ProjectileType.Wind,
      WIND_WIZARD_ATTACK_DAMAGE,
      "/projectile/base.png"
    )
  )

  private val wizardConfigs: Map[WizardType, WizardConfig] = Map(
    WizardType.Generator -> WizardConfig(
      wizardType = WizardType.Generator,
      health = GENERATOR_WIZARD_HEALTH,
      cost = GENERATOR_WIZARD_COST,
      imagePath = "/wizard/generator.png",
      elixirGenerator = Some(ElixirGeneratorComponent(
        GENERATOR_WIZARD_ELIXIR_PER_SECOND,
        GENERATOR_WIZARD_COOLDOWN
      ))
    ),
    WizardType.Wind -> WizardConfig(
      wizardType = WizardType.Wind,
      health = WIND_WIZARD_HEALTH,
      cost = WIND_WIZARD_COST,
      imagePath = "/wizard/wind.png",
      attack = Some(AttackComponent(
        WIND_WIZARD_ATTACK_DAMAGE,
        WIND_WIZARD_RANGE,
        WIND_WIZARD_COOLDOWN
      ))
    ),
    WizardType.Barrier -> WizardConfig(
      wizardType = WizardType.Barrier,
      health = BARRIER_WIZARD_HEALTH,
      cost = BARRIER_WIZARD_COST,
      imagePath = "/wizard/barrier.png"
    ),
    WizardType.Fire -> WizardConfig(
      wizardType = WizardType.Fire,
      health = FIRE_WIZARD_HEALTH,
      cost = FIRE_WIZARD_COST,
      imagePath = "/wizard/fire.png",
      attack = Some(AttackComponent(
        FIRE_WIZARD_ATTACK_DAMAGE,
        FIRE_WIZARD_RANGE,
        FIRE_WIZARD_COOLDOWN
      ))
    ),
    WizardType.Ice -> WizardConfig(
      wizardType = WizardType.Ice,
      health = ICE_WIZARD_HEALTH,
      cost = ICE_WIZARD_COST,
      imagePath = "/wizard/ice.png",
      attack = Some(AttackComponent(
        ICE_WIZARD_ATTACK_DAMAGE,
        ICE_WIZARD_RANGE,
        ICE_WIZARD_COOLDOWN
      ))
    )
  )

  private val trollConfigs: Map[TrollType, TrollConfig] = Map(
    TrollType.Base -> TrollConfig(
      trollType = TrollType.Base,
      health = BASE_TROLL_HEALTH,
      speed = BASE_TROLL_SPEED,
      damage = BASE_TROLL_DAMAGE,
      range = BASE_TROLL_RANGE,
      cooldown = BASE_TROLL_COOLDOWN,
      imagePath = "/troll/BaseTroll.png"
    ),
    TrollType.Warrior -> TrollConfig(
      trollType = TrollType.Warrior,
      health = WARRIOR_TROLL_HEALTH,
      speed = WARRIOR_TROLL_SPEED,
      damage = WARRIOR_TROLL_DAMAGE,
      range = WARRIOR_TROLL_RANGE,
      cooldown = WARRIOR_TROLL_COOLDOWN,
      imagePath = "/troll/WarriorTroll.png"
    ),
    TrollType.Assassin -> TrollConfig(
      trollType = TrollType.Assassin,
      health = ASSASSIN_TROLL_HEALTH,
      speed = ASSASSIN_TROLL_SPEED,
      damage = ASSASSIN_TROLL_DAMAGE,
      range = ASSASSIN_TROLL_RANGE,
      cooldown = ASSASSIN_TROLL_COOLDOWN,
      imagePath = "/troll/Assassin.png"
    ),
    TrollType.Thrower -> TrollConfig(
      trollType = TrollType.Thrower,
      health = THROWER_TROLL_HEALTH,
      speed = THROWER_TROLL_SPEED,
      damage = THROWER_TROLL_DAMAGE,
      range = THROWER_TROLL_RANGE,
      cooldown = THROWER_TROLL_COOLDOWN,
      imagePath = "/troll/ThrowerTroll.png"
    )
  )

  /** Creates an entity with components, returning the updated World and entity ID. */
  private def createEntity[T: EntityBuilder](
      world: World,
      position: Position,
      config: T
  ): (World, EntityId) =
    val (world1, entity) = world.createEntity()
    val builder          = summon[EntityBuilder[T]]
    val components       = builder.buildComponents(config, position)

    val finalWorld = components.foldLeft(world1): (w, component) =>
      w.addComponent(entity, component)

    (finalWorld, entity)

  def createProjectile(world: World, position: Position, projectileType: ProjectileType): (World, EntityId) =
    projectileConfigs.get(projectileType) match
      case Some(config) => createEntity(world, position, config)
      case None         => throw IllegalArgumentException(s"Unknown projectile type: $projectileType")

  private def createWizard(world: World, position: Position, wizardType: WizardType): (World, EntityId) =
    wizardConfigs.get(wizardType) match
      case Some(config) => createEntity(world, position, config)
      case None         => throw IllegalArgumentException(s"Unknown wizard type: $wizardType")

  private def createTroll(world: World, position: Position, trollType: TrollType): (World, EntityId) =
    trollConfigs.get(trollType) match
      case Some(config) => createEntity(world, position, config)
      case None         => throw IllegalArgumentException(s"Unknown troll type: $trollType")

  def createGeneratorWizard(world: World, position: Position): (World, EntityId) =
    createWizard(world, position, WizardType.Generator)

  def createWindWizard(world: World, position: Position): (World, EntityId) =
    createWizard(world, position, WizardType.Wind)

  def createBarrierWizard(world: World, position: Position): (World, EntityId) =
    createWizard(world, position, WizardType.Barrier)

  def createFireWizard(world: World, position: Position): (World, EntityId) =
    createWizard(world, position, WizardType.Fire)

  def createIceWizard(world: World, position: Position): (World, EntityId) =
    createWizard(world, position, WizardType.Ice)

  def createBaseTroll(world: World, pos: Position): (World, EntityId) =
    createTroll(world, pos, TrollType.Base)

  def createWarriorTroll(world: World, pos: Position): (World, EntityId) =
    createTroll(world, pos, TrollType.Warrior)

  def createAssassinTroll(world: World, pos: Position): (World, EntityId) =
    createTroll(world, pos, TrollType.Assassin)

  def createThrowerTroll(world: World, pos: Position): (World, EntityId) =
    createTroll(world, pos, TrollType.Thrower)
