package it.unibo.pps.wvt.ecs.core

import it.unibo.pps.wvt.ecs.components.{HealthComponent, PositionComponent, TrollType, TrollTypeComponent, WizardType, WizardTypeComponent}
import it.unibo.pps.wvt.utilities.Position
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WorldTest extends AnyFlatSpec with Matchers:

  behavior of "An empty World"

  it should "have size 0 and be empty" in:
    val world = World.empty
    world.size shouldBe 0
    world.isEmpty shouldBe true
    world.getAllEntities shouldBe empty

  it should "allow creating a new entity" in:
    val (world, entity) = World.empty.createEntity()
    world.size shouldBe 1
    world.isEmpty shouldBe false
    world.getAllEntities should contain(entity)

  behavior of "A World with entities"

  it should "allow destroying an entity and its components" in:
    val (worldWithEntity, entity) = World.empty.createEntity()
    val worldWithComponent = worldWithEntity.addComponent(entity, PositionComponent(Position(0, 0)))

    val destroyedWorld = worldWithComponent.destroyEntity(entity)
    destroyedWorld.size shouldBe 0
    destroyedWorld.getAllEntities should not contain entity
    destroyedWorld.getComponent[PositionComponent](entity) shouldBe None

  it should "allow adding a component to an entity" in:
    val (world, entity) = World.empty.createEntity()
    val position = Position(10, 20)
    val worldWithComponent = world.addComponent(entity, PositionComponent(position))

    worldWithComponent.getComponent[PositionComponent](entity) shouldBe Some(PositionComponent(position))
    worldWithComponent.hasComponent[PositionComponent](entity) shouldBe true

  it should "allow removing a component from an entity" in:
    val (world, entity) = World.empty.createEntity()
    val worldWithComponent = world.addComponent(entity, PositionComponent(Position(0, 0)))
    val worldWithoutComponent = worldWithComponent.removeComponent[PositionComponent](entity)

    worldWithoutComponent.getComponent[PositionComponent](entity) shouldBe None
    worldWithoutComponent.hasComponent[PositionComponent](entity) shouldBe false

  it should "allow updating an existing component" in:
    val (world, entity) = World.empty.createEntity()
    val initialPos = Position(0, 0)
    val updatedPos = Position(10, 10)
    val worldWithComponent = world.addComponent(entity, PositionComponent(initialPos))

    val updatedWorld = worldWithComponent.updateComponent[PositionComponent](entity, _ => PositionComponent(updatedPos))
    updatedWorld.getComponent[PositionComponent](entity).map(_.position) shouldBe Some(updatedPos)

  it should "return all entities that have a specific component" in:
    val (world, entity1) = World.empty.createEntity()
    val (world2, entity2) = world.createEntity()
    val world3 = world2.addComponent(entity1, PositionComponent(Position(0, 0)))
    val world4 = world3.addComponent(entity2, HealthComponent(100, 100))

    world4.getEntitiesWithComponent[PositionComponent] should contain only entity1
    world4.getEntitiesWithComponent[HealthComponent] should contain only entity2

  it should "return entities that have two specific components" in:
    val (world, e1) = World.empty.createEntity()
    val (world2, e2) = world.createEntity()
    val (world3, e3) = world2.createEntity()

    val finalWorld = world3
      .addComponent(e1, PositionComponent(Position(0, 0)))
      .addComponent(e1, HealthComponent(100, 100))
      .addComponent(e2, PositionComponent(Position(1, 1)))
      .addComponent(e3, HealthComponent(50, 50))

    val entities = finalWorld.getEntitiesWithTwoComponents[PositionComponent, HealthComponent]
    entities should contain only e1

  it should "correctly categorize and retrieve entities by type" in:
    val (world, wizard) = World.empty.createEntity()
    val (world2, troll) = world.createEntity()

    val finalWorld = world2
      .addComponent(wizard, WizardTypeComponent(WizardType.Fire))
      .addComponent(troll, TrollTypeComponent(TrollType.Base))

    finalWorld.getEntitiesByType("wizard") should contain only wizard
    finalWorld.getEntitiesByType("troll") should contain only troll
    finalWorld.getEntitiesByType("projectile") shouldBe empty

  it should "find an entity at a specific grid location" in:
    val (world, entity) = World.empty.createEntity()
    val position = Position(580, 200) // Approx cell (0, 0)
    val worldWithEntity = world.addComponent(entity, PositionComponent(position))

    worldWithEntity.getEntityAt(position) shouldBe Some(entity)
    worldWithEntity.getEntityAt(Position(0, 0)) shouldBe None

  it should "confirm if a wizard is at a specific position" in:
    val (world, wizardEntity) = World.empty.createEntity()
    val (world2, trollEntity) = world.createEntity()
    val position = Position(600, 200)

    val finalWorld = world2
      .addComponent(wizardEntity, PositionComponent(position))
      .addComponent(wizardEntity, WizardTypeComponent(WizardType.Ice))
      .addComponent(trollEntity, PositionComponent(Position(700, 200)))
      .addComponent(trollEntity, TrollTypeComponent(TrollType.Base))

    finalWorld.hasWizardAt(position) shouldBe true
    finalWorld.hasWizardAt(Position(700, 200)) shouldBe false

  it should "clear all entities and components" in:
    val (world, entity) = World.empty.createEntity()
    val finalWorld = world.addComponent(entity, PositionComponent(Position(0, 0)))

    val clearedWorld = finalWorld.clear()
    clearedWorld.isEmpty shouldBe true
    clearedWorld.size shouldBe 0