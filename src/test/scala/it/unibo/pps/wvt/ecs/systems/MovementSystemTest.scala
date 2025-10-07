package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import it.unibo.pps.wvt.utilities.ViewConstants.*

import it.unibo.pps.wvt.utilities.{Position, Position, TestConstants}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

class MovementSystemTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  // Test DSL for readable test setup
  private object TestDSL:
    extension (world: World)
      def withTroll(trollType: TrollType, position: Position, speed: Double): World =
        val troll = world.createEntity()
        world.addComponent(troll, TrollTypeComponent(trollType))
        world.addComponent(troll, PositionComponent(position))
        world.addComponent(troll, MovementComponent(speed))
        world

      def withProjectile(projectileType: ProjectileType, position: Position, speed: Double): World =
        val projectile = world.createEntity()
        world.addComponent(projectile, ProjectileTypeComponent(projectileType))
        world.addComponent(projectile, PositionComponent(position))
        world.addComponent(projectile, MovementComponent(speed))
        world

      def getTrollAt(position: Position): Option[PositionComponent] =
        world.getEntitiesByType("troll")
          .headOption
          .flatMap(world.getComponent[PositionComponent])

      def getProjectileAt: Option[PositionComponent] =
        world.getEntitiesByType("projectile")
          .headOption
          .flatMap(world.getComponent[PositionComponent])

      def getFirstEntityPosition: Option[PositionComponent] =
        world.getAllEntities
          .headOption
          .flatMap(world.getComponent[PositionComponent])

  import TestDSL.*

  behavior of "MovementSystem - Base Troll Movement"

  it should "move base trolls straight left with normal speed" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Base, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_NORMAL)
    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL))
    newPos shouldBe defined
    newPos.get.position.toPixel.x should be < Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL).toPixel.x

  it should "move base trolls with slow speed" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Base, Position(TEST_MIDDLE_ROW, TEST_START_COL), TEST_SPEED_SLOW)
    val initialX = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL)).get.position.toPixel.x

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL))
    newPos.get.position.toPixel.x should be < initialX

  it should "move base trolls with fast speed" in:
    val world = World()
    val testDeltaTime = TEST_DELTA_TIME
    val system = MovementSystem(deltaTime = testDeltaTime)

    world.withTroll(Base, Position(TEST_MIDDLE_ROW, TEST_START_COL), TEST_SPEED_FAST)
    val initialX = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL)).get.position.toPixel.x

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL))
    val displacement = initialX - newPos.get.position.toPixel.x
    displacement should be > (TEST_SPEED_NORMAL * CELL_WIDTH * (testDeltaTime / 1000.0) * 0.9)

  behavior of "MovementSystem - Warrior Troll Movement"

  it should "move warrior trolls straight left" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Warrior, Position(TEST_MIDDLE_ROW, TEST_START_COL), TEST_SPEED_FAST)
    val initialX = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL)).get.position.toPixel.x

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL))
    newPos.get.position.toPixel.x should be < initialX
    newPos.get.position.toPixel.y shouldBe Position(TEST_MIDDLE_ROW, TEST_START_COL).toPixel.y

  behavior of "MovementSystem - Assassin Troll Movement"

  it should "move assassin trolls in zigzag pattern" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Assassin, Position(TEST_MIDDLE_ROW, TEST_START_COL), TEST_SPEED_NORMAL)
    val initialPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL)).get.position.toPixel

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL)).get.position.toPixel
    newPos.x should be < initialPos.x
    // Y position should change due to zigzag (sin wave)
    newPos.y should not equal initialPos.y

  it should "handle assassin zigzag at top boundary" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Assassin, Position(TEST_TOP_BOUNDARY, TEST_MIDDLE_COL), TEST_SPEED_NORMAL)

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_TOP_BOUNDARY, TEST_MIDDLE_COL))
    newPos.get.position.toPixel.y should be >= GRID_OFFSET_Y

  it should "handle assassin zigzag at bottom boundary" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Assassin, Position(TEST_BOTTOM_BOUNDARY, TEST_MIDDLE_COL), TEST_SPEED_NORMAL)

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_BOTTOM_BOUNDARY, TEST_MIDDLE_COL))
    newPos.get.position.toPixel.y should be <= (GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2)

  behavior of "MovementSystem - Thrower Troll Movement"

  it should "move thrower trolls until stop column" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Thrower, Position(TEST_MIDDLE_ROW, TEST_START_COL), TEST_SPEED_NORMAL)
    val initialX = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL)).get.position.toPixel.x

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_START_COL))
    newPos.get.position.toPixel.x should be < initialX

  it should "stop thrower trolls at stop column" in:
    val world = World()
    val system = MovementSystem()
    val stopX = GRID_OFFSET_X + CELL_WIDTH * TEST_THROWER_STOP_COL

    world.withTroll(Thrower, Position(TEST_MIDDLE_ROW, TEST_THROWER_STOP_COL), TEST_SPEED_NORMAL)
    val troll = world.getEntitiesByType("troll").head
    world.addComponent(troll, PositionComponent(Position(stopX - 1, Position(TEST_MIDDLE_ROW, TEST_THROWER_STOP_COL).toPixel.y)))

    system.update(world)

    val newPos = world.getComponent[PositionComponent](troll)
    newPos.get.position.toPixel.x should be >= stopX - CELL_WIDTH

  behavior of "MovementSystem - Projectile Movement"

  it should "move troll projectiles left" in:
    val world = World()
    val system = MovementSystem()

    world.withProjectile(ProjectileType.Troll, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_FAST)
    val initialX = world.getProjectileAt.get.position.toPixel.x

    system.update(world)

    val newPos = world.getProjectileAt
    newPos.get.position.toPixel.x should be < initialX

  it should "move wizard projectiles right" in:
    val world = World()
    val system = MovementSystem()

    world.withProjectile(ProjectileType.Fire, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_NORMAL)
    val initialX = world.getProjectileAt.get.position.toPixel.x

    system.update(world)

    val newPos = world.getProjectileAt
    newPos.get.position.toPixel.x should be > initialX

  it should "move ice projectiles right" in:
    val world = World()
    val system = MovementSystem()

    world.withProjectile(ProjectileType.Ice, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_FAST)
    val initialX = world.getProjectileAt.get.position.toPixel.x

    system.update(world)

    val newPos = world.getProjectileAt
    newPos.get.position.toPixel.x should be > initialX

  it should "move wind projectiles right" in:
    val world = World()
    val system = MovementSystem()

    world.withProjectile(ProjectileType.Wind, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_NORMAL)
    val initialX = world.getProjectileAt.get.position.toPixel.x

    system.update(world)

    val newPos = world.getProjectileAt
    newPos.get.position.toPixel.x should be > initialX

  behavior of "MovementSystem - Boundary Handling"

  it should "remove troll projectiles at left boundary" in:
    val world = World()
    val system = MovementSystem()

    val projectile = world.createEntity()
    world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Troll))
    world.addComponent(projectile, PositionComponent(Position(GRID_OFFSET_X - 10, GRID_OFFSET_Y + CELL_HEIGHT)))
    world.addComponent(projectile, MovementComponent(TEST_SPEED_NORMAL))

    system.update(world)

    world.getEntitiesByType("projectile") shouldBe empty

  it should "remove wizard projectiles at right boundary" in:
    val world = World()
    val system = MovementSystem()

    val projectile = world.createEntity()
    world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Fire))
    world.addComponent(projectile, PositionComponent(Position(GRID_OFFSET_X + GRID_COLS * CELL_WIDTH + 10, GRID_OFFSET_Y + CELL_HEIGHT)))
    world.addComponent(projectile, MovementComponent(TEST_SPEED_NORMAL))

    system.update(world)

    world.getEntitiesByType("projectile") shouldBe empty

  it should "constrain projectiles vertically within grid" in:
    val world = World()
    val system = MovementSystem()

    world.withProjectile(ProjectileType.Fire, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_NORMAL)
    val projectile = world.getEntitiesByType("projectile").head
    world.addComponent(projectile, PositionComponent(Position(GRID_OFFSET_X + CELL_WIDTH * 3, GRID_OFFSET_Y - 10)))

    system.update(world)

    val newPos = world.getComponent[PositionComponent](projectile)
    newPos.get.position.toPixel.y should be >= GRID_OFFSET_Y

  behavior of "MovementSystem - Special Cases"

  it should "not move entities with zero speed" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Base, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_ZERO)
    val initialPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL)).get.position.toPixel

    system.update(world)

    val newPos = world.getTrollAt(Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL))
    newPos.get.position.toPixel shouldBe initialPos

  it should "not move entities without movement component" in:
    val world = World()
    val system = MovementSystem()

    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL)))
    val initialPos = Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL)

    system.update(world)

    val newPos = world.getFirstEntityPosition
    newPos.get.position shouldBe initialPos

  it should "not move entities without position component" in:
    val world = World()
    val system = MovementSystem()

    val entity = world.createEntity()
    world.addComponent(entity, MovementComponent(TEST_SPEED_NORMAL))

    system.update(world)

    world.getComponent[PositionComponent](entity) shouldBe None

  behavior of "MovementSystem - Multiple Entities"

  it should "move multiple trolls simultaneously" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Base, Position(0, TEST_START_COL), TEST_SPEED_NORMAL)
    world.withTroll(Warrior, Position(1, TEST_START_COL), TEST_SPEED_FAST)
    world.withTroll(Assassin, Position(2, TEST_START_COL), TEST_SPEED_SLOW)

    system.update(world)

    val trolls = world.getEntitiesByType("troll")
    trolls.size shouldBe 3
    trolls.foreach: troll =>
      val pos = world.getComponent[PositionComponent](troll)
      pos shouldBe defined

  it should "move mixed entity types correctly" in:
    val world = World()
    val system = MovementSystem()

    world.withTroll(Base, Position(TEST_MIDDLE_ROW, TEST_START_COL), TEST_SPEED_NORMAL)
    world.withProjectile(ProjectileType.Fire, Position(TEST_MIDDLE_ROW, TEST_MIDDLE_COL), TEST_SPEED_FAST)

    system.update(world)

    val trolls = world.getEntitiesByType("troll")
    val projectiles = world.getEntitiesByType("projectile")

    trolls.size shouldBe 1
    projectiles.size shouldBe 1

  behavior of "MovementSystem - System State"

  it should "return itself after update" in:
    val world = World()
    val system = MovementSystem()

    val returnedSystem = system.update(world)

    returnedSystem shouldBe a[MovementSystem]
    returnedSystem shouldBe system