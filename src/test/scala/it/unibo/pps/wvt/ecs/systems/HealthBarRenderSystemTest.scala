package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.controller.GameScenarioDSL.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.TestConstants.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter
import scalafx.scene.paint.Color

class HealthBarRenderSystemTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  var world: World                           = _
  var healthBarSystem: HealthBarRenderSystem = _

  before:
    world = World.empty
    healthBarSystem = HealthBarRenderSystem()

  behavior of "HealthBarRenderSystem"

  it should "render health bars for wizards with health less than 100%" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val wizard = testWorld.getEntitiesByType("wizard").head
    val damagedWorld = testWorld.updateComponent[HealthComponent](
      wizard,
      h =>
        HealthComponent(HEALTH_MID, HEALTH_FULL)
    )

    val (_, updatedSystem) = healthBarSystem.update(damagedWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars should have size ENTITY_COUNT_SINGLE
    val (_, percentage, color, _, _, _) = healthBars.head
    percentage shouldBe 0.5 +- EPSILON
    color shouldBe Color.Yellow

  it should "not render health bars for entities at full health" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val (_, updatedSystem) = healthBarSystem.update(testWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars shouldBe empty

  it should "not render health bars for dead entities" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val wizard = testWorld.getEntitiesByType("wizard").head
    val deadWorld = testWorld.updateComponent[HealthComponent](
      wizard,
      h =>
        HealthComponent(HEALTH_ZERO, HEALTH_FULL)
    )

    val (_, updatedSystem) = healthBarSystem.update(deadWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars shouldBe empty

  it should "render health bars for trolls with red color" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Base).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val troll = testWorld.getEntitiesByType("troll").head
    val damagedWorld = testWorld.updateComponent[HealthComponent](
      troll,
      h =>
        HealthComponent(HEALTH_LOW, HEALTH_FULL)
    )

    val (_, updatedSystem) = healthBarSystem.update(damagedWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars should have size ENTITY_COUNT_SINGLE
    val (_, percentage, color, _, _, _) = healthBars.head
    percentage shouldBe 0.25 +- EPSILON
    color shouldBe Color.Red

  it should "create default health bar for wizard without explicit health bar component" in:
    val pos              = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COL_START).get
    val (world1, wizard) = world.createEntity()
    val testWorld = world1
      .addComponent(wizard, WizardTypeComponent(WizardType.Fire))
      .addComponent(wizard, PositionComponent(pos))
      .addComponent(wizard, HealthComponent(HEALTH_MID, HEALTH_FULL))

    val (_, updatedSystem) = healthBarSystem.update(testWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars should have size ENTITY_COUNT_SINGLE
    val (_, _, color, _, _, _) = healthBars.head
    color shouldBe Color.Yellow

  it should "create default health bar for troll without explicit health bar component" in:
    val pos             = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COL_END).get
    val (world1, troll) = world.createEntity()
    val testWorld = world1
      .addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(pos))
      .addComponent(troll, HealthComponent(HEALTH_MID, HEALTH_FULL))

    val (_, updatedSystem) = healthBarSystem.update(testWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars should have size ENTITY_COUNT_SINGLE
    val (_, _, color, _, _, _) = healthBars.head
    color shouldBe Color.Yellow

  it should "render multiple health bars for multiple damaged entities" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_START, GRID_COL_START)
        .withWizard(WizardType.Ice).at(GRID_ROW_MID, GRID_COL_START)
        .withTroll(TrollType.Base).at(GRID_ROW_END, GRID_COL_END)
        .withElixir(ELIXIR_HIGH)

    val wizards = testWorld.getEntitiesByType("wizard").toList
    val trolls  = testWorld.getEntitiesByType("troll").toList

    var damagedWorld = testWorld
    wizards.foreach: wizard =>
      damagedWorld = damagedWorld.updateComponent[HealthComponent](
        wizard,
        h =>
          HealthComponent(HEALTH_MID, HEALTH_FULL)
      )

    trolls.foreach: troll =>
      damagedWorld = damagedWorld.updateComponent[HealthComponent](
        troll,
        h =>
          HealthComponent(HEALTH_LOW, HEALTH_FULL)
      )

    val (_, updatedSystem) = healthBarSystem.update(damagedWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars should have size ENTITY_COUNT_FEW

  it should "update health bar color based on health percentage" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val wizard = testWorld.getEntitiesByType("wizard").head
    val damagedWorld = testWorld.updateComponent[HealthComponent](
      wizard,
      h =>
        HealthComponent(HEALTH_LOW, HEALTH_FULL)
    )

    val (_, updatedSystem) = healthBarSystem.update(damagedWorld)
    val healthBars         = updatedSystem.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender

    healthBars should have size ENTITY_COUNT_SINGLE
    val (_, percentage, _, _, _, _) = healthBars.head
    percentage shouldBe 0.25 +- EPSILON

  it should "maintain health bar cache across updates" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val wizard = testWorld.getEntitiesByType("wizard").head
    val damagedWorld = testWorld.updateComponent[HealthComponent](
      wizard,
      h =>
        HealthComponent(HEALTH_MID, HEALTH_FULL)
    )

    val (_, firstUpdate)  = healthBarSystem.update(damagedWorld)
    val (_, secondUpdate) = firstUpdate.update(damagedWorld)

    firstUpdate.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender should have size ENTITY_COUNT_SINGLE
    secondUpdate.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender should have size ENTITY_COUNT_SINGLE

  it should "clear health bar cache when entity is healed to full" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withWizard(WizardType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val wizard = testWorld.getEntitiesByType("wizard").head
    val damagedWorld = testWorld.updateComponent[HealthComponent](
      wizard,
      h =>
        HealthComponent(HEALTH_MID, HEALTH_FULL)
    )

    val (_, firstUpdate) = healthBarSystem.update(damagedWorld)
    firstUpdate.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender should have size ENTITY_COUNT_SINGLE

    val healedWorld = damagedWorld.updateComponent[HealthComponent](
      wizard,
      h =>
        HealthComponent(HEALTH_FULL, HEALTH_FULL)
    )

    val (_, secondUpdate) = firstUpdate.update(healedWorld)
    secondUpdate.asInstanceOf[HealthBarRenderSystem].getHealthBarsToRender shouldBe empty
