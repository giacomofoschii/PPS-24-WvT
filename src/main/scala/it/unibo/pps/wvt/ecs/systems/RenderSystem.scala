package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.utilities.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.view.GameView

case class RenderSystem(
                         private val healthBarSystem: HealthBarRenderSystem = HealthBarRenderSystem()
                       ) extends System:

  private var lastRenderedState: Option[String] = None

  override def update(world: World): System =
    val updatedHealthBars = healthBarSystem.update(world).asInstanceOf[HealthBarRenderSystem]
    val entities = collectEntitiesWithImages(world)
    val healthBars = updatedHealthBars.getHealthBarsToRender
    val currentState = generateStateHash(entities, healthBars)

    if shouldRender(currentState) then
      GameView.clearGrid()
      GameView.renderEntities(entities)
      GameView.renderHealthBars(healthBars)
      lastRenderedState = Some(currentState)
      copy(healthBarSystem = updatedHealthBars)
    else
      copy(healthBarSystem = updatedHealthBars)

  private def shouldRender(currentState: String): Boolean =
    !lastRenderedState.contains(currentState)

  private def generateStateHash(
                                 entities: Seq[(GridMapper.PhysicalCoords, String)],
                                 healthBars: Seq[(GridMapper.PhysicalCoords, Double, scalafx.scene.paint.Color, Double, Double, Double)]
                               ): String =
    val entitiesHash = entities.map { case ((x, y), path) => s"$x,$y,$path" }.mkString(";")
    val healthHash = healthBars.map { case ((x, y), p, _, _, _, _) => s"$x,$y,$p" }.mkString(";")
    s"$entitiesHash|$healthHash"

  private def collectEntitiesWithImages(world: World): Seq[(GridMapper.PhysicalCoords, String)] =
    val wizardEntities = world.getEntitiesByType("wizard").flatMap: entity =>
      for
        pos <- world.getComponent[PositionComponent](entity)
        wizardType <- world.getComponent[WizardTypeComponent](entity)
      yield (GridMapper.logicalToPhysical(pos.position), getWizardImagePath(wizardType.wizardType))

    val trollEntities = world.getEntitiesByType("troll").flatMap: entity =>
      for
        pos <- world.getComponent[PositionComponent](entity)
        trollType <- world.getComponent[TrollTypeComponent](entity)
      yield (GridMapper.logicalToPhysical(pos.position), getTrollImagePath(trollType.trollType))
    (wizardEntities ++ trollEntities).toSeq

  def getWizardImagePath(wizardType: WizardType): String = wizardType match
    case WizardType.Generator => "/wizard/generator.png"
    case WizardType.Wind => "/wizard/wind.png"
    case WizardType.Barrier => "/wizard/barrier.png"
    case WizardType.Fire => "/wizard/fire.png"
    case WizardType.Ice => "/wizard/ice.png"

  private def getTrollImagePath(trollType: TrollType): String = trollType match
    case TrollType.Base => "/troll/BASE_TROLL/WALK_005.png"
    case TrollType.Warrior => "/troll/WAR_TROLL/WALK_005.png"
    case TrollType.Assassin => "/troll/Assassin.png"
    case TrollType.Thrower => "/troll/THROW_TROLL/WALK_005.png"

  def forceRender(): Unit =
    lastRenderedState = None

  def clearCache(): Unit =
    lastRenderedState = None