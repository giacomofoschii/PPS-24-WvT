package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.ecs.core.EntityId

case class ElixirSystem(
                         totalElixir: Int = INITIAL_ELIXIR,
                         lastPeriodicGeneration: Long = 0L,
                         firstWizardPlaced: Boolean = false
                       ) extends System:

  override def update(world: World): System =
    if firstWizardPlaced then
      updatePeriodicElixirGeneration()
        .updateGeneratorWizardElixir(world)
    else
      this

  private def updatePeriodicElixirGeneration(): ElixirSystem =
    val currentTime = System.currentTimeMillis()
    if lastPeriodicGeneration > 0 && currentTime - lastPeriodicGeneration >= ELIXIR_GENERATION_INTERVAL then
      copy(
        totalElixir = totalElixir + PERIODIC_ELIXIR,
        lastPeriodicGeneration = currentTime
      )
    else if lastPeriodicGeneration == 0L then
      copy(lastPeriodicGeneration = currentTime)
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
        cooldownComponent <- world.getComponent[CooldownComponent](entityId).orElse(Some(CooldownComponent(0L)))
      yield
        if wizardType.wizardType == WizardType.Generator then
          val isJustPlaced = cooldownComponent.remainingTime == 0L
          val cooldownExpired = cooldownComponent.remainingTime <= currentTime

          if cooldownExpired then
            if !isJustPlaced then
              updatedSystem = updatedSystem.copy(totalElixir = updatedSystem.totalElixir + elixirGenerator.elixirPerSecond)
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
    copy(firstWizardPlaced = true, lastPeriodicGeneration = 0L)

  def reset(): ElixirSystem =
    copy(totalElixir = INITIAL_ELIXIR, lastPeriodicGeneration = 0L, firstWizardPlaced = false)