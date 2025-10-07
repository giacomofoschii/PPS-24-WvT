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
    if firstWizardPlaced then
      updatePeriodicElixirGeneration()
        .updateGeneratorWizardElixir(world)
    else
      this

  private def updatePeriodicElixirGeneration(): ElixirSystem =
    val currentTime = System.currentTimeMillis()
    if lastPeriodicGeneration == 0L then
      return copy(
        lastPeriodicGeneration = currentTime,
        activationTime = if activationTime == 0L then currentTime else activationTime
      )
    val timeSinceActivation = currentTime - activationTime
    val timeSinceLastGeneration = currentTime - lastPeriodicGeneration
    if timeSinceActivation >= ELIXIR_GENERATION_INTERVAL &&
      timeSinceLastGeneration >= ELIXIR_GENERATION_INTERVAL then
      copy(
        totalElixir = totalElixir + PERIODIC_ELIXIR,
        lastPeriodicGeneration = currentTime
      )
    else
      this

  private def updateGeneratorWizardElixir(world: World): ElixirSystem =
    val generatorEntities = world.getEntitiesWithTwoComponents[WizardTypeComponent, ElixirGeneratorComponent]
    val currentTime = System.currentTimeMillis()
    var updatedSystem = this

    generatorEntities.foreach: entityId =>
      for
        wizardType <- world.getComponent[WizardTypeComponent](entityId)
        elixirGenerator <- world.getComponent[ElixirGeneratorComponent](entityId)
      yield
        if wizardType.wizardType == WizardType.Generator then
          world.getComponent[CooldownComponent](entityId) match
            case Some(cooldownComponent) =>
              if cooldownComponent.remainingTime == 0L then
                updateCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
              else if cooldownComponent.remainingTime <= currentTime then
                updatedSystem = updatedSystem.copy(totalElixir = updatedSystem.totalElixir + elixirGenerator.elixirPerSecond)
                updateCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
            case None =>
              updateCooldown(world, entityId, currentTime + elixirGenerator.cooldown)
    updatedSystem

  private def updateCooldown(world: World, entityId: EntityId, newTime: Long): Unit =
    if world.hasComponent[CooldownComponent](entityId) then
      world.removeComponent[CooldownComponent](entityId)
    world.addComponent(entityId, CooldownComponent(newTime))

  def spendElixir(amount: Int): (ElixirSystem, Boolean) =
    if totalElixir >= amount then
      (copy(totalElixir = totalElixir - amount), true)
    else (this, false)

  def addElixir(amount: Int): ElixirSystem =
    copy(totalElixir = totalElixir + amount)

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