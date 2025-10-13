package it.unibo.pps.wvt.ecs.core

import it.unibo.pps.wvt.ecs.components._
import it.unibo.pps.wvt.utilities.{GridMapper, Position}

import scala.annotation.tailrec

case class World(
                private val entities: Set[EntityId] = Set.empty,
                private val components: Map[Class[_], Map[EntityId, Component]] = Map.empty,
                private val entitiesByType: Map[String, Set[EntityId]] = Map.empty
                ):

  def createentity(): (World, EntityId) =
    val entity = EntityId.generate()
    (copy(entities = entities + entity), entity)

  def destroyEntity(entity: EntityId): World =
    copy(
      entities = entities - entity,
      components = components.map((cmp, entityMap) => cmp -> (entityMap - entity)),
      entitiesByType = entitiesByType.map((etype, entitySet) => etype -> (entitySet - entity))
    )

  def addComponent[T <: Component](entity: EntityId, component: T): World =
    if !entities.contains(entity) then
      this
    else
      val componentClass = component.getClass
      val updatedComponents = components.updatedWith(componentClass): opt =>
        Some(opt.getOrElse(Map.empty) + (entity -> component))

      val updatedEntitiesByType = updateEntityTypeMapping(entity, component)

      copy(components = updatedComponents, entitiesByType = updatedEntitiesByType)

  def removeComponent[T <: Component](entityId: EntityId)(using ct: reflect.ClassTag[T]): World =
    val componentClass = ct.runtimeClass
    val updatedComponents = components.updatedWith(componentClass):
      case Some(entityMap) =>
        val newEntityMap = entityMap - entityId
        if newEntityMap.isEmpty then None else Some(newEntityMap)
      case None => None

    copy(components = updatedComponents)

  def updateComponent[T <: Component](entity: EntityId, updateFn: T => T)(using ct: reflect.ClassTag[T]): World =
    val componentClass = ct.runtimeClass
    getComponent[T](entity) match
      case Some(component) =>
        val updatedComponent = updateFn(component)
        removeComponent[T](entity).addComponent(entity, updatedComponent)
      case None =>
        this

  def getComponent[T <: Component](entity: EntityId)(using ct: reflect.ClassTag[T]): Option[T] =
    components.get(ct.runtimeClass).flatMap(_.get(entity).map(_.asInstanceOf[T]))

  def hasComponent[T <: Component](entity: EntityId)(using ct: reflect.ClassTag[T]): Boolean =
    getComponent[T](entity).isDefined

  def getEntitiesWithComponent[T <: Component](using ct: reflect.ClassTag[T]): Set[EntityId] =
    components.get(ct.runtimeClass).map(_.keySet).getOrElse(Set.empty)

  def getEntititesWithTwoComponents[A <: Component, B <: Component]
  (using ctA: reflect.ClassTag[A], ctB: reflect.ClassTag[B]): Set[EntityId] =
    val entitiesA = getEntitiesWithComponent[A]
    val entitiesB = getEntitiesWithComponent[B]
    entitiesA.intersect(entitiesB)

  def getEntitiesByType(entityType: String): Set[EntityId] =
    entitiesByType.getOrElse(entityType, Set.empty)

  def getAllEntities: Set[EntityId] = entities

  def getEntityAt(position: Position): Option[EntityId] =
    GridMapper.physicalToLogical(position).flatMap: targetGrid =>

      @tailrec
      def findEntityRecursive(remaining: List[EntityId]): Option[EntityId] =
        remaining match
          case Nil => None
          case head :: tail =>
            getComponent[PositionComponent](head) match
              case Some(posComp) =>
                GridMapper.physicalToLogical(posComp.position) match
                  case Some(entityGrid) if entityGrid == targetGrid =>
                    Some(head)
                  case _ => findEntityRecursive(tail)
              case _ => findEntityRecursive(tail)

      val entitiesList = getEntitiesWithComponent[PositionComponent].toList
      findEntityRecursive(entitiesList)

  private def getEntitiesAt(position: Position): Seq[EntityId] =
    for
      targetGrid <- GridMapper.physicalToLogical(position).toSeq
      entity <- getEntitiesWithComponent[PositionComponent].toSeq
      posComp <- getComponent[PositionComponent](entity)
      entityGrid <- GridMapper.physicalToLogical(posComp.position)
      if entityGrid == targetGrid
    yield entity

  def hasWizardAt(position: Position): Boolean =
    getEntitiesAt(position).exists(entity => hasComponent[WizardTypeComponent](entity))

  def getEntityType(entity: EntityId): Option[EntityTypeComponent] =
    getComponent[WizardTypeComponent](entity)
      .orElse(getComponent[TrollTypeComponent](entity))
      .orElse(getComponent[ProjectileTypeComponent](entity))

  def clear(): World =
    World()

  def size: Int = entities.size

  def isEmpty: Boolean = entities.isEmpty

  private def updateEntityTypeMapping(entity: EntityId, component: Component): Map[String, Set[EntityId]] =
    component match
      case _: WizardTypeComponent =>
        entitiesByType.updatedWith("Wizard"): opt =>
          Some(opt.getOrElse(Set.empty) + entity)
      case _: TrollTypeComponent =>
        entitiesByType.updatedWith("Troll"): opt =>
          Some(opt.getOrElse(Set.empty) + entity)
      case _: ProjectileTypeComponent =>
        entitiesByType.updatedWith("Projectile"): opt =>
          Some(opt.getOrElse(Set.empty) + entity)
      case _ => entitiesByType

object World:

  def empty: World = World()

  def withEntities(entities: EntityId*): World =
    World(entities = entities.toSet)