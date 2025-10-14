package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.ecs.core.EntityId

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

  private def checkAndGenerateElixir(currentTime: Long): Option[ElixirSystem] =
    val timeSinceActivation = currentTime - activationTime
    val timeSinceLastGeneration = currentTime - lastPeriodicGeneration
    Option.when(
      timeSinceActivation >= ELIXIR_GENERATION_INTERVAL &&
        timeSinceLastGeneration >= ELIXIR_GENERATION_INTERVAL
    ):
      addElixir(PERIODIC_ELIXIR)
        .copy(lastPeriodicGeneration = currentTime)

  private def updateGeneratorWizardElixir(world: World): (World, ElixirSystem) =
    val currentTime = System.currentTimeMillis()
    val generatorEntities = world.getEntitiesWithTwoComponents[WizardTypeComponent, ElixirGeneratorComponent].toList

    generatorEntities.foldLeft((world, this)): (acc, entityId) =>
      val (currentWorld, currentSystem) = acc
      processGeneratorEntity(currentWorld, entityId, currentTime, currentSystem)

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

  private def setCooldown(world: World, entityId: EntityId, newTime: Long): World =
    val worldWithoutCooldown = world.getComponent[CooldownComponent](entityId) match
      case Some(_) => world.removeComponent[CooldownComponent](entityId)
      case None => world
    worldWithoutCooldown.addComponent(entityId, CooldownComponent(newTime))

  def spendElixir(amount: Int): (ElixirSystem, Boolean) =
    Option.when(totalElixir >= amount):
      copy(totalElixir = totalElixir - amount)
    .map((_, true))
    .getOrElse((this, false))

  def addElixir(amount: Int): ElixirSystem =
    copy(totalElixir = Math.min(totalElixir + amount, MAX_ELIXIR))

  def getCurrentElixir: Int = totalElixir

  def canAfford(amount: Int): Boolean = totalElixir >= amount

  def activateGeneration(): ElixirSystem =
    val currentTime = System.currentTimeMillis()
    copy(
      firstWizardPlaced = true,
      lastPeriodicGeneration = currentTime,
      activationTime = currentTime
    )

  def reset(): ElixirSystem =
    copy(
      totalElixir = INITIAL_ELIXIR,
      lastPeriodicGeneration = 0L,
      firstWizardPlaced = false,
      activationTime = 0L
    )
