package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.config.WaveLevel
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.factories.EntityFactory
import it.unibo.pps.wvt.ecs.systems.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}

object GameScenarioDSL:

  def scenario(setup: ScenarioBuilder => Unit): (World, GameSystemsState) =
    val builder = ScenarioBuilder()
    setup(builder)
    builder.build()

  class ScenarioBuilder:
    private var world           = World.empty
    private var elixir          = 200
    private var wave            = 1
    private var elixirGenActive = false
    private var applyScaling    = false

    def withWizard(wizardType: WizardType): WizardPlacer =
      WizardPlacer(this, wizardType)

    def withTroll(trollType: TrollType): TrollPlacer =
      TrollPlacer(this, trollType)

    def withProjectile(projectileType: ProjectileType): ProjectilePlacer =
      ProjectilePlacer(this, projectileType)

    def withElixir(amount: Int): this.type =
      elixir = amount
      this

    def atWave(waveNumber: Int): this.type =
      wave = waveNumber
      this

    def activateElixirGeneration(): this.type =
      elixirGenActive = true
      this

    def applyWaveScaling(): this.type =
      applyScaling = true
      this

    private[GameScenarioDSL] def placeWizard(wizardType: WizardType, row: Int, col: Int): Unit =
      val pos = GridMapper.logicalToPhysical(row, col).get
      val (newWorld, _) = wizardType match
        case WizardType.Generator => EntityFactory.createGeneratorWizard(world, pos)
        case WizardType.Wind      => EntityFactory.createWindWizard(world, pos)
        case WizardType.Barrier   => EntityFactory.createBarrierWizard(world, pos)
        case WizardType.Fire      => EntityFactory.createFireWizard(world, pos)
        case WizardType.Ice       => EntityFactory.createIceWizard(world, pos)
      world = newWorld

    private[GameScenarioDSL] def placeTroll(trollType: TrollType, row: Int, col: Int): Unit =
      val pos = GridMapper.logicalToPhysical(row, col).get
      val (newWorld, entityId) = trollType match
        case TrollType.Base     => EntityFactory.createBaseTroll(world, pos)
        case TrollType.Warrior  => EntityFactory.createWarriorTroll(world, pos)
        case TrollType.Assassin => EntityFactory.createAssassinTroll(world, pos)
        case TrollType.Thrower  => EntityFactory.createThrowerTroll(world, pos)

      world = if applyScaling then applyWaveScalingToEntity(newWorld, entityId, trollType)
      else newWorld

    private[GameScenarioDSL] def placeProjectile(projType: ProjectileType, row: Int, col: Int): Unit =
      val pos           = GridMapper.logicalToPhysical(row, col).get
      val (newWorld, _) = EntityFactory.createProjectile(world, pos, projType)
      world = newWorld

    private def applyWaveScalingToEntity(w: World, entity: EntityId, trollType: TrollType): World =
      import it.unibo.pps.wvt.utilities.GamePlayConstants.*

      val (baseHealth, baseSpeed, baseDamage) = trollType match
        case TrollType.Base     => (BASE_TROLL_HEALTH, BASE_TROLL_SPEED, BASE_TROLL_DAMAGE)
        case TrollType.Warrior  => (WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_SPEED, WARRIOR_TROLL_DAMAGE)
        case TrollType.Assassin => (ASSASSIN_TROLL_HEALTH, ASSASSIN_TROLL_SPEED, ASSASSIN_TROLL_DAMAGE)
        case TrollType.Thrower  => (THROWER_TROLL_HEALTH, THROWER_TROLL_SPEED, THROWER_TROLL_DAMAGE)

      val (scaledHealth, scaledSpeed, scaledDamage) =
        WaveLevel.applyMultipliers(baseHealth, baseSpeed, baseDamage, wave)

      var updatedWorld = w
      updatedWorld =
        updatedWorld.updateComponent[HealthComponent](entity, _ => HealthComponent(scaledHealth, scaledHealth))
      updatedWorld = updatedWorld.updateComponent[MovementComponent](entity, _ => MovementComponent(scaledSpeed))
      updatedWorld = updatedWorld.updateComponent[AttackComponent](
        entity,
        old =>
          AttackComponent(scaledDamage, old.range, old.cooldown)
      )

      updatedWorld

    def build(): (World, GameSystemsState) =
      val elixirSystem = if elixirGenActive then
        ElixirSystem(totalElixir = elixir).activateGeneration()
      else
        ElixirSystem(totalElixir = elixir)

      val state = GameSystemsState.initial(wave).copy(
        elixir = elixirSystem,
        health = HealthSystem(elixirSystem, Set.empty)
      )

      (world, state)

  case class WizardPlacer(builder: ScenarioBuilder, wizardType: WizardType):
    def at(row: Int, col: Int): ScenarioBuilder =
      builder.placeWizard(wizardType, row, col)
      builder

  case class TrollPlacer(builder: ScenarioBuilder, trollType: TrollType):
    def at(row: Int, col: Int): ScenarioBuilder =
      builder.placeTroll(trollType, row, col)
      builder

  case class ProjectilePlacer(builder: ScenarioBuilder, projectileType: ProjectileType):
    def at(row: Int, col: Int): ScenarioBuilder =
      builder.placeProjectile(projectileType, row, col)
      builder
