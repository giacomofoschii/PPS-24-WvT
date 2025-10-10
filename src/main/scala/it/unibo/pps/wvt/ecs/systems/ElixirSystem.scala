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

  

  override def update(world: World): System =
    Option.when(firstWizardPlaced):
      updatePeriodicElixirGeneration()
        .updateGeneratorWizardElixir(world)
    .getOrElse(this)

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
      val elixirToAdd = Math.min(PERIODIC_ELIXIR, MAX_ELIXIR - totalElixir)
      copy(
        totalElixir = totalElixir + elixirToAdd,
        lastPeriodicGeneration = currentTime
      )

  private def updateGeneratorWizardElixir(world: World): ElixirSystem =
    val currentTime = System.currentTimeMillis()
    val generatorEntities = world.getEntitiesWithTwoComponents[WizardTypeComponent, ElixirGeneratorComponent].toList
    generatorEntities.foldLeft(this): (system, entityId) =>
      processGeneratorEntity(world, entityId, currentTime, system)

  private def processGeneratorEntity(
                                      world: World,
                                      entityId: EntityId,
                                      currentTime: Long,
                                      system: ElixirSystem
                                    ): ElixirSystem =
    (for
      wizardType <- world.getComponent[WizardTypeComponent](entityId)
      _ <- Option.when(wizardType.wizardType == WizardType.Generator)(())
      elixirGenerator <- world.getComponent[ElixirGeneratorComponent](entityId)
    yield
      processGeneratorCooldown(world, entityId, currentTime, elixirGenerator, system)
      ).getOrElse(system)

  private def processGeneratorCooldown(
                                        world: World,
                                        entityId: EntityId,
                                        currentTime: Long,
                                        elixirGenerator: ElixirGeneratorComponent,
                                        system: ElixirSystem
                                      ): ElixirSystem =
    world.getComponent[CooldownComponent](entityId)
      .map: cooldown =>
        handleCooldownState(world, entityId, currentTime, elixirGenerator, cooldown, system)
      .getOrElse:
        setCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
        system

  private def handleCooldownState(
                                   world: World,
                                   entityId: EntityId,
                                   currentTime: Long,
                                   elixirGenerator: ElixirGeneratorComponent,
                                   cooldown: CooldownComponent,
                                   system: ElixirSystem
                                 ): ElixirSystem =
    cooldown.remainingTime match
      case 0L =>
        setCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
        system
      case time if time <= currentTime =>
        setCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
        val elixirToAdd = Math.min(elixirGenerator.elixirPerSecond, MAX_ELIXIR - system.totalElixir)
        system.copy(totalElixir = system.totalElixir + elixirToAdd)
      case _ =>
        system

  private def setCooldown(world: World, entityId: EntityId, newTime: Long): Unit =
    world.getComponent[CooldownComponent](entityId).foreach: _ =>
      world.removeComponent[CooldownComponent](entityId)
    world.addComponent(entityId, CooldownComponent(newTime))

  def spendElixir(amount: Int): (ElixirSystem, Boolean) =
    Option.when(totalElixir >= amount):
      copy(totalElixir = totalElixir - amount)
    .map((_, true))
    .getOrElse((this, false))

  def addElixir(amount: Int): ElixirSystem =
    val elixirToAdd = Math.min(amount, MAX_ELIXIR - totalElixir)
    copy(totalElixir = totalElixir + elixirToAdd)

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