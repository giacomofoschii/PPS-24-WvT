package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.ecs.core.EntityId

/**
 *
 * @param totalElixir stores the currentElixir amount
 * @param lastPeriodicGeneration stores the last time when elixir was generated
 * @param firstWizardPlaced flag that says if the game has started
 * @param activationTime store the first elixir generation time
 *
 */
case class ElixirSystem(
                         totalElixir: Int = INITIAL_ELIXIR,
                         lastPeriodicGeneration: Long = 0L,
                         firstWizardPlaced: Boolean = false,
                         activationTime: Long = 0L
                       ) extends System:

  override def update(world: World): (World, System) =
    Option.when(firstWizardPlaced):
      val periodicSystem = updatePeriodicElixirGeneration()
      periodicSystem.updateGeneratorWizardElixir(world)
    .getOrElse((world, this))

  /**
   * Updates the elixir generation based on time intervals.
   * Generates elixir periodically if the game has started and enough time has passed.
   * @return Updated ElixirSystem with new elixir amount and timestamps.
   */
  private def updatePeriodicElixirGeneration(): ElixirSystem =
    val currentTime = System.currentTimeMillis()
    Option.when(lastPeriodicGeneration == 0L):
      copy(
        lastPeriodicGeneration = currentTime,
        activationTime = Option.when(activationTime == 0L)(currentTime).getOrElse(activationTime)
      )
    .orElse:
      checkAndGenerateElixir(currentTime)
    .getOrElse(this)
  /**
   * Checks if enough time has passed since the last elixir generation and generates elixir if so.
   * @param currentTime The current system time in milliseconds.
   * @return Option containing updated ElixirSystem if elixir was generated, None otherwise.
   */
  private def checkAndGenerateElixir(currentTime: Long): Option[ElixirSystem] =
    val timeSinceActivation = currentTime - activationTime
    val timeSinceLastGeneration = currentTime - lastPeriodicGeneration
    Option.when(
      timeSinceActivation >= ELIXIR_GENERATION_INTERVAL &&
        timeSinceLastGeneration >= ELIXIR_GENERATION_INTERVAL
    ):
      addElixir(PERIODIC_ELIXIR)
        .copy(lastPeriodicGeneration = currentTime)
  /**
   * Updates the elixir generation for all wizard entities with elixir generators.
   * @param world The current game world containing entities and components.
   * @return A tuple containing the updated world and ElixirSystem.
   */
  private def updateGeneratorWizardElixir(world: World): (World, ElixirSystem) =
    val currentTime = System.currentTimeMillis()
    val generatorEntities = world.getEntitiesWithTwoComponents[WizardTypeComponent, ElixirGeneratorComponent].toList

    generatorEntities.foldLeft((world, this)): (acc, entityId) =>
      val (currentWorld, currentSystem) = acc
      processGeneratorEntity(currentWorld, entityId, currentTime, currentSystem)

  /**
   * @param world the actual world, with all the entities
   * @param entityId the Generator entity to process
   * @param currentTime the actual time
   * @param system the actual elixir system
   * @return the updated world and elixir system
   */
  private def processGeneratorEntity(
                                      world: World,
                                      entityId: EntityId,
                                      currentTime: Long,
                                      system: ElixirSystem
                                    ): (World, ElixirSystem) =
    (for
      wizardType <- world.getComponent[WizardTypeComponent](entityId)
      _ <- Option.when(wizardType.wizardType == WizardType.Generator)(())
      elixirGenerator <- world.getComponent[ElixirGeneratorComponent](entityId)
    yield
      processGeneratorCooldown(world, entityId, currentTime, elixirGenerator, system)
      ).getOrElse((world, system))
  /**
   * Processes the cooldown of an elixir generator entity.
   * If the cooldown has expired, generates elixir and resets the cooldown.
   * @param world The current game world.
   * @param entityId The entity ID of the elixir generator.
   * @param currentTime The current system time in milliseconds.
   * @param elixirGenerator The ElixirGeneratorComponent of the entity.
   * @param system The current ElixirSystem.
   * @return A tuple containing the updated world and ElixirSystem.
   */
  private def processGeneratorCooldown(
                                        world: World,
                                        entityId: EntityId,
                                        currentTime: Long,
                                        elixirGenerator: ElixirGeneratorComponent,
                                        system: ElixirSystem
                                      ): (World, ElixirSystem) =
    world.getComponent[CooldownComponent](entityId)
      .map: cooldown =>
        handleCooldownState(world, entityId, currentTime, elixirGenerator, cooldown, system)
      .getOrElse:
        val updatedWorld = setCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
        (updatedWorld, system)

  /**
   * Handles the state of the cooldown for an elixir generator.
   * @param world the actual world
   * @param entityId the generator to process
   * @param currentTime the actual time
   * @param elixirGenerator the elixir generator component
   * @param cooldown the cooldown component
   * @param system the actual elixir system
   * @return the updated world and elixir system
   */
  private def handleCooldownState(
                                   world: World,
                                   entityId: EntityId,
                                   currentTime: Long,
                                   elixirGenerator: ElixirGeneratorComponent,
                                   cooldown: CooldownComponent,
                                   system: ElixirSystem
                                 ): (World, ElixirSystem) =
    cooldown.remainingTime match
      case 0L =>
        val updatedWorld = setCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
        (updatedWorld, system)
      case time if time <= currentTime =>
        val updatedWorld = setCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
        (updatedWorld, system.addElixir(elixirGenerator.elixirPerSecond))
      case _ =>
        (world, system)

  /**
   * @param world the actual world
   * @param entityId the entity to set the cooldown
   * @param newTime the new cooldown time
   * @return the updated world with the new cooldown
   */
  private def setCooldown(world: World, entityId: EntityId, newTime: Long): World =
    val worldWithoutCooldown = world.getComponent[CooldownComponent](entityId) match
      case Some(_) => world.removeComponent[CooldownComponent](entityId)
      case None => world
    worldWithoutCooldown.addComponent(entityId, CooldownComponent(newTime))
  /**
   * Attempts to spend a specified amount of elixir.
   * @param amount The amount of elixir to spend.
   * @return A tuple containing the updated ElixirSystem and a boolean indicating success.
   */
  def spendElixir(amount: Int): (ElixirSystem, Boolean) =
    Option.when(totalElixir >= amount):
      copy(totalElixir = totalElixir - amount)
    .map((_, true))
    .getOrElse((this, false))
  /**
   * Adds a specified amount of elixir, ensuring it does not exceed the maximum limit.
   * @param amount The amount of elixir to add.
   * @return The updated ElixirSystem with the new elixir amount.
   */
  def addElixir(amount: Int): ElixirSystem =
    copy(totalElixir = Math.min(totalElixir + amount, MAX_ELIXIR))
  /**
   * Retrieves the current amount of elixir.
   * @return The current elixir amount.
   */
  def getCurrentElixir: Int = totalElixir

  /**
   * Checks if there is enough elixir to afford a specified amount.
   * @param amount The amount of elixir to check.
   * @return True if there is enough elixir, false otherwise.
   */
  def canAfford(amount: Int): Boolean = totalElixir >= amount
  /**
   * Activates the elixir generation system when the first wizard is placed.
   * Sets the activation time and initializes the last generation timestamp.
   * @return The updated ElixirSystem with generation activated.
   */
  def activateGeneration(): ElixirSystem =
    val currentTime = System.currentTimeMillis()
    copy(
      firstWizardPlaced = true,
      lastPeriodicGeneration = currentTime,
      activationTime = currentTime
    )

