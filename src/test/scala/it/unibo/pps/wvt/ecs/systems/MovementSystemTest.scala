package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.controller.GameScenarioDSL.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.TestConstants.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.ViewConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

class MovementSystemTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  var world: World                   = _
  var movementSystem: MovementSystem = _

  before:
    world = World.empty
    movementSystem = MovementSystem(deltaTime = DELTA_TIME_MS)

  behavior of "MovementSystem"

  it should "move trolls leftward" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Base).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val troll      = testWorld.getEntitiesByType("troll").head
    val initialPos = testWorld.getComponent[PositionComponent](troll).get.position

    val (updatedWorld, _) = movementSystem.update(testWorld)
    val newPos            = updatedWorld.getComponent[PositionComponent](troll).get.position

    newPos.x should be < initialPos.x
    newPos.y shouldBe initialPos.y +- MOVEMENT_TOLERANCE

  it should "move wizard projectiles rightward" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withProjectile(ProjectileType.Fire).at(GRID_ROW_MID, GRID_COL_START)
        .withElixir(ELIXIR_START)

    val projectile = testWorld.getEntitiesByType("projectile").head
    val initialPos = testWorld.getComponent[PositionComponent](projectile).get.position

    val (updatedWorld, _) = movementSystem.update(testWorld)
    val newPos            = updatedWorld.getComponent[PositionComponent](projectile).get.position

    newPos.x should be > initialPos.x
    newPos.y shouldBe initialPos.y +- MOVEMENT_TOLERANCE

  it should "move troll projectiles leftward" in:
    val pos                  = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COL_MID).get
    val (world1, projectile) = world.createEntity()
    val testWorld = world1
      .addComponent(projectile, PositionComponent(pos))
      .addComponent(projectile, MovementComponent(SPEED_NORMAL))
      .addComponent(projectile, ProjectileTypeComponent(ProjectileType.Troll))

    val initialPos = testWorld.getComponent[PositionComponent](projectile).get.position

    val (updatedWorld, _) = movementSystem.update(testWorld)
    val newPos            = updatedWorld.getComponent[PositionComponent](projectile).get.position

    newPos.x should be < initialPos.x

  it should "not move entities with BlockedComponent" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Base).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val troll        = testWorld.getEntitiesByType("troll").head
    val blockedWorld = testWorld.addComponent(troll, BlockedComponent(troll))
    val initialPos   = blockedWorld.getComponent[PositionComponent](troll).get.position

    val (updatedWorld, _) = movementSystem.update(blockedWorld)
    val newPos            = updatedWorld.getComponent[PositionComponent](troll).get.position

    newPos.x shouldBe initialPos.x +- EPSILON
    newPos.y shouldBe initialPos.y +- EPSILON

  it should "reduce movement speed when entity is frozen" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Base).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val troll       = testWorld.getEntitiesByType("troll").head
    val frozenWorld = testWorld.addComponent(troll, FreezedComponent(remainingTime = 1000L, speedModifier = 0.5))

    val initialPos        = frozenWorld.getComponent[PositionComponent](troll).get.position
    val (updatedWorld, _) = movementSystem.update(frozenWorld)
    val newPos            = updatedWorld.getComponent[PositionComponent](troll).get.position

    val movement = initialPos.x - newPos.x
    movement should be > 0.0

    // Now test normal movement for comparison
    val (normalWorld, _) = movementSystem.update(testWorld)
    val normalNewPos     = normalWorld.getComponent[PositionComponent](troll).get.position
    val normalMovement   = initialPos.x - normalNewPos.x

    movement should be < normalMovement

  it should "initialize zigzag state for assassin trolls" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Assassin).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val assassin = testWorld.getEntitiesByType("troll").head
    testWorld.hasComponent[ZigZagStateComponent](assassin) shouldBe false

    val (updatedWorld, _) = movementSystem.update(testWorld)
    updatedWorld.hasComponent[ZigZagStateComponent](assassin) shouldBe true

    val zigzagState = updatedWorld.getComponent[ZigZagStateComponent](assassin).get
    zigzagState.currentPhase shouldBe ZigZagPhase.OnSpawnRow

  it should "move assassin trolls in zigzag pattern" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Assassin).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val assassin   = testWorld.getEntitiesByType("troll").head
    val initialPos = testWorld.getComponent[PositionComponent](assassin).get.position

    // Initialize zigzag state
    val (world1, _) = movementSystem.update(testWorld)

    // Move multiple times to see zigzag pattern
    var currentWorld = world1
    (1 to UPDATES_COUNT_MEDIUM).foreach: _ =>
      val (nextWorld, _) = movementSystem.update(currentWorld)
      currentWorld = nextWorld

    val finalPos = currentWorld.getComponent[PositionComponent](assassin).get.position
    finalPos.x should be < initialPos.x

  it should "remove projectiles that go out of bounds to the right" in:
    val pos                  = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COLS_LOGICAL - 1).get
    val (world1, projectile) = world.createEntity()
    val testWorld = world1
      .addComponent(projectile, PositionComponent(pos))
      .addComponent(projectile, MovementComponent(SPEED_VERY_FAST))
      .addComponent(projectile, ProjectileTypeComponent(ProjectileType.Fire))

    testWorld.getAllEntities should contain(projectile)

    var currentWorld = testWorld
    (1 to UPDATES_PER_SECOND).foreach: _ =>
      val (nextWorld, _) = movementSystem.update(currentWorld)
      currentWorld = nextWorld

    currentWorld.getAllEntities should not contain projectile

  it should "remove troll projectiles that go out of bounds to the left" in:
    val pos                  = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COL_START).get
    val (world1, projectile) = world.createEntity()
    val testWorld = world1
      .addComponent(projectile, PositionComponent(pos))
      .addComponent(projectile, MovementComponent(SPEED_VERY_FAST))
      .addComponent(projectile, ProjectileTypeComponent(ProjectileType.Troll))

    testWorld.getAllEntities should contain(projectile)

    var currentWorld = testWorld
    (1 to UPDATES_PER_SECOND).foreach: _ =>
      val (nextWorld, _) = movementSystem.update(currentWorld)
      currentWorld = nextWorld

    currentWorld.getAllEntities should not contain projectile

  it should "not move projectiles beyond grid bounds vertically" in:
    val pos                  = Position(POS_X_MID, GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT)
    val (world1, projectile) = world.createEntity()
    val testWorld = world1
      .addComponent(projectile, PositionComponent(pos))
      .addComponent(projectile, MovementComponent(SPEED_NORMAL))
      .addComponent(projectile, ProjectileTypeComponent(ProjectileType.Fire))

    val (updatedWorld, _) = movementSystem.update(testWorld)
    val newPos            = updatedWorld.getComponent[PositionComponent](projectile).get.position

    newPos.y should be <= (GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2)

  it should "handle multiple trolls moving simultaneously" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Base).at(GRID_ROW_START, GRID_COL_END)
        .withTroll(TrollType.Warrior).at(GRID_ROW_MID, GRID_COL_END)
        .withTroll(TrollType.Assassin).at(GRID_ROW_END, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val (worldAfterInit, _) = movementSystem.update(testWorld)

    val trolls = worldAfterInit.getEntitiesByType("troll").toList
    val initialPositions = trolls.map: troll =>
      worldAfterInit.getComponent[PositionComponent](troll).get.position

    val (updatedWorld, _) = movementSystem.update(worldAfterInit)

    trolls.zip(initialPositions).foreach: (troll, initialPos) =>
      val newPos = updatedWorld.getComponent[PositionComponent](troll).get.position
      newPos.x should be < initialPos.x

  it should "handle entities without movement component gracefully" in:
    val pos              = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COL_MID).get
    val (world1, entity) = world.createEntity()
    val testWorld        = world1.addComponent(entity, PositionComponent(pos))

    val initialPos = testWorld.getComponent[PositionComponent](entity).get.position

    val (updatedWorld, _) = movementSystem.update(testWorld)
    val newPos            = updatedWorld.getComponent[PositionComponent](entity).get.position

    newPos.x shouldBe initialPos.x +- EPSILON
    newPos.y shouldBe initialPos.y +- EPSILON

  it should "transition zigzag phase after duration" in:
    val movementSystemWithShortDuration = MovementSystem(
      deltaTime = DELTA_TIME_MS,
      zigZagPhaseDuration = 100L
    )

    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Assassin).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val assassin = testWorld.getEntitiesByType("troll").head

    // Initialize zigzag
    val (world1, _)  = movementSystemWithShortDuration.update(testWorld)
    val initialPhase = world1.getComponent[ZigZagStateComponent](assassin).get.currentPhase

    // Wait and update to trigger phase change
    Thread.sleep(150)

    var currentWorld = world1
    (1 to UPDATES_COUNT_MEDIUM).foreach: _ =>
      val (nextWorld, _) = movementSystemWithShortDuration.update(currentWorld)
      currentWorld = nextWorld

    val finalPhase = currentWorld.getComponent[ZigZagStateComponent](assassin).get.currentPhase
    finalPhase should not equal initialPhase

  it should "calculate alternate row correctly for zigzag at grid boundaries" in:
    val (testWorldTop, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Assassin).at(GRID_ROW_START, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val assassinTop    = testWorldTop.getEntitiesByType("troll").head
    val (worldTop1, _) = movementSystem.update(testWorldTop)
    val zigzagStateTop = worldTop1.getComponent[ZigZagStateComponent](assassinTop).get

    zigzagStateTop.alternateRow shouldBe 1

    val (testWorldBottom, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Assassin).at(GRID_ROW_NEAR_END, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val assassinBottom    = testWorldBottom.getEntitiesByType("troll").head
    val (worldBottom1, _) = movementSystem.update(testWorldBottom)
    val zigzagStateBottom = worldBottom1.getComponent[ZigZagStateComponent](assassinBottom).get

    zigzagStateBottom.alternateRow shouldBe (GRID_ROW_NEAR_END - 1)

  it should "keep warrior trolls moving in straight line" in:
    val (testWorld, _) = scenario: builder =>
      builder
        .withTroll(TrollType.Warrior).at(GRID_ROW_MID, GRID_COL_END)
        .withElixir(ELIXIR_START)

    val warrior    = testWorld.getEntitiesByType("troll").head
    val initialPos = testWorld.getComponent[PositionComponent](warrior).get.position

    var currentWorld = testWorld
    (1 to UPDATES_COUNT_MEDIUM).foreach: _ =>
      val (nextWorld, _) = movementSystem.update(currentWorld)
      currentWorld = nextWorld

    val finalPos = currentWorld.getComponent[PositionComponent](warrior).get.position

    finalPos.x should be < initialPos.x
    finalPos.y shouldBe initialPos.y +- MOVEMENT_TOLERANCE
    currentWorld.hasComponent[ZigZagStateComponent](warrior) shouldBe false
