package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.TestConstants.*
import scalafx.scene.paint.Color
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HealthBarRenderSystemTest extends AnyFlatSpec with Matchers:

  // Test DSL for HealthBarRenderSystem
  private object HealthBarTestDSL:
    extension (world: World)
      def createEntityWithHealth(health: Int, maxHealth: Int, withWizardType: Boolean = false): EntityId =
        val entity = world.createEntity()
        world.addComponent(entity, PositionComponent(Position(TEST_HEALTH_BAR_X, TEST_HEALTH_BAR_Y)))
        world.addComponent(entity, HealthComponent(health, maxHealth))
        if withWizardType then
          world.addComponent(entity, WizardTypeComponent(WizardType.Fire))
        entity

      def createWizardWithHealth(health: Int, maxHealth: Int, position: Position = Position(TEST_HEALTH_BAR_X, TEST_HEALTH_BAR_Y)): EntityId =
        val entity = world.createEntity()
        world.addComponent(entity, PositionComponent(position))
        world.addComponent(entity, HealthComponent(health, maxHealth))
        world.addComponent(entity, WizardTypeComponent(WizardType.Wind))
        entity

      def createTrollWithHealth(health: Int, maxHealth: Int): EntityId =
        val entity = world.createEntity()
        world.addComponent(entity, PositionComponent(Position(TEST_HEALTH_BAR_X, TEST_HEALTH_BAR_Y)))
        world.addComponent(entity, HealthComponent(health, maxHealth))
        world.addComponent(entity, TrollTypeComponent(TrollType.Base))
        entity

      def damageEntity(entity: EntityId, damage: Int): Unit =
        world.getComponent[HealthComponent](entity).foreach: health =>
          val newHealth = math.max(TEST_ENTITY_DEAD_HEALTH, health.currentHealth - damage)
          world.updateComponent[HealthComponent](entity, _ => health.copy(currentHealth = newHealth))

    extension (system: HealthBarRenderSystem)
      def updateAndGetBars(world: World): Seq[RenderableHealthBar] =
        val updated = system.update(world).asInstanceOf[HealthBarRenderSystem]
        updated.getHealthBarsToRender

      def getBarCount(world: World): Int =
        updateAndGetBars(world).size

  import HealthBarTestDSL.*

  behavior of "HealthBarRenderSystem - Basic Functionality"

  it should "start with empty cache" in:
    val system = HealthBarRenderSystem()

    system.getHealthBarsToRender shouldBe empty

  it should "render health bar for entity with partial health" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_HALF_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    val bars = system.updateAndGetBars(world)

    bars should have size TEST_ONE_BAR

  it should "not render health bar for entity at full health" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_MAX_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    val bars = system.updateAndGetBars(world)

    bars shouldBe empty

  it should "not render health bar for dead entity" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_ENTITY_DEAD_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    val bars = system.updateAndGetBars(world)

    bars shouldBe empty

  behavior of "HealthBarRenderSystem - Color Updates"

  it should "use green color for high health percentage" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_HIGH_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    val bars = system.updateAndGetBars(world)

    bars should have size TEST_ONE_BAR
    val (_, percentage, color, _, _, _) = bars.head
    percentage should be > TEST_HEALTH_BAR_GREEN_THRESHOLD
    color shouldBe Color.Green

  it should "use yellow color for medium health percentage" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_MEDIUM_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    val bars = system.updateAndGetBars(world)

    bars should have size TEST_ONE_BAR
    val (_, percentage, color, _, _, _) = bars.head
    percentage should be > TEST_HEALTH_BAR_RED_THRESHOLD
    percentage should be <= TEST_HEALTH_BAR_GREEN_THRESHOLD
    color shouldBe Color.Yellow

  it should "use red color for low health percentage" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_LOW_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    val bars = system.updateAndGetBars(world)

    bars should have size TEST_ONE_BAR
    val (_, percentage, color, _, _, _) = bars.head
    percentage should be <= TEST_HEALTH_BAR_RED_THRESHOLD
    color shouldBe Color.Red

  behavior of "HealthBarRenderSystem - Multiple Entities"

  it should "render health bars for multiple damaged entities" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_HALF_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createEntityWithHealth(TEST_HEALTH_BAR_LOW_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createEntityWithHealth(TEST_HEALTH_BAR_MEDIUM_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)

    val bars = system.updateAndGetBars(world)
    bars should have size TEST_THREE_BARS

  it should "filter out full health entities from multiple entities" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_MAX_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createEntityWithHealth(TEST_HEALTH_BAR_HALF_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createEntityWithHealth(TEST_HEALTH_BAR_MAX_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createEntityWithHealth(TEST_HEALTH_BAR_LOW_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)

    val bars = system.updateAndGetBars(world)
    bars should have size TEST_TWO_BARS

  behavior of "HealthBarRenderSystem - Average Health Calculation"

  it should "calculate average health for wizards correctly" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createWizardWithHealth(TEST_HEALTH_BAR_HALF_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createWizardWithHealth(TEST_HEALTH_BAR_MAX_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)

    val updated = system.update(world).asInstanceOf[HealthBarRenderSystem]
    val avgHealth = updated.getAverageHealth(world, "wizard")

    avgHealth shouldBe defined
    avgHealth.get shouldBe TEST_HEALTH_BAR_AVG_WIZARD_HEALTH +- TEST_HEALTH_BAR_TOLERANCE

  it should "calculate average health for trolls correctly" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createTrollWithHealth(TEST_HEALTH_BAR_LOW_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createTrollWithHealth(TEST_HEALTH_BAR_MEDIUM_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    world.createTrollWithHealth(TEST_HEALTH_BAR_HIGH_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)

    val updated = system.update(world).asInstanceOf[HealthBarRenderSystem]
    val avgHealth = updated.getAverageHealth(world, "troll")

    avgHealth shouldBe defined
    avgHealth.get shouldBe TEST_HEALTH_BAR_AVG_TROLL_HEALTH +- TEST_HEALTH_BAR_TOLERANCE

  it should "return None for average health when no entities exist" in:
    val world = World()
    val system = HealthBarRenderSystem()

    val updated = system.update(world).asInstanceOf[HealthBarRenderSystem]
    val avgHealth = updated.getAverageHealth(world, "wizard")

    avgHealth shouldBe None

  behavior of "HealthBarRenderSystem - Edge Cases"

  it should "handle entity without health component" in:
    val world = World()
    val system = HealthBarRenderSystem()

    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(Position(TEST_HEALTH_BAR_X, TEST_HEALTH_BAR_Y)))

    val bars = system.updateAndGetBars(world)
    bars shouldBe empty

  it should "handle entity without position component" in:
    val world = World()
    val system = HealthBarRenderSystem()

    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(TEST_HEALTH_BAR_HALF_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH))

    val bars = system.updateAndGetBars(world)
    bars shouldBe empty

  it should "handle empty world without errors" in:
    val world = World()
    val system = HealthBarRenderSystem()

    noException should be thrownBy system.update(world)
    system.getHealthBarsToRender shouldBe empty

  behavior of "HealthBarRenderSystem - Health Bar Properties"

  it should "calculate correct health percentage" in:
    val world = World()
    val system = HealthBarRenderSystem()

    world.createEntityWithHealth(TEST_HEALTH_BAR_QUARTER_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)
    val bars = system.updateAndGetBars(world)

    bars should have size TEST_ONE_BAR
    val (_, percentage, _, _, _, _) = bars.head
    percentage shouldBe TEST_HEALTH_BAR_QUARTER_PERCENTAGE +- TEST_HEALTH_BAR_TOLERANCE

  it should "maintain correct position for health bar" in:
    val world = World()
    val system = HealthBarRenderSystem()
    val testPos = Position(TEST_HEALTH_BAR_CUSTOM_X, TEST_HEALTH_BAR_CUSTOM_Y)

    world.createWizardWithHealth(TEST_HEALTH_BAR_HALF_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH, testPos)
    val bars = system.updateAndGetBars(world)

    bars should have size TEST_ONE_BAR
    val (position, _, _, _, _, _) = bars.head
    position shouldBe testPos

  behavior of "HealthBarRenderSystem - Dynamic Health Changes"

  it should "update health bar when entity takes damage" in:
    val world = World()
    val system = HealthBarRenderSystem()

    val entity = world.createEntityWithHealth(TEST_HEALTH_BAR_MAX_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)

    // Initially no bar (full health)
    system.getBarCount(world) shouldBe TEST_NO_BARS

    // Damage entity
    world.damageEntity(entity, TEST_HEALTH_BAR_DAMAGE_MEDIUM)

    // Now bar should appear
    system.getBarCount(world) shouldBe TEST_ONE_BAR

  it should "remove health bar when entity reaches full health" in:
    val world = World()
    val system = HealthBarRenderSystem()

    val entity = world.createEntityWithHealth(TEST_HEALTH_BAR_HALF_HEALTH, TEST_HEALTH_BAR_MAX_HEALTH)

    // Initially has bar
    system.getBarCount(world) shouldBe TEST_ONE_BAR

    // Heal to full
    world.updateComponent[HealthComponent](entity, h => h.copy(currentHealth = h.maxHealth))

    // Bar should disappear
    system.getBarCount(world) shouldBe TEST_NO_BARS