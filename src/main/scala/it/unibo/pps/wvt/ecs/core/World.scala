package it.unibo.pps.wvt.ecs.core

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.*

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.Buffer
import scala.reflect.ClassTag

class World:
  private val entities = mutable.Buffer[EntityId]()
  private val components = mutable.Map[Class[_], mutable.Map[EntityId, Component]]()
  private val entitiesByType = mutable.Map[String, mutable.Buffer[EntityId]]()

  def createEntity(): EntityId =
    val entity = EntityId.generate()
    entities.append(entity)
    entity

  def destroyEntity(entity: EntityId): Unit =
    entities --= Seq(entity)
    components.values.foreach(_.remove(entity))
    entitiesByType.values.foreach(_.--=(Seq(entity)))

  def addComponent[T <: Component](entity: EntityId, component: T): Unit =
    if(!entities.contains(entity)) throw new IllegalArgumentException(s"Entity $entity does not exist.")
    val store = components.getOrElseUpdate(component.getClass, mutable.Map[EntityId, Component]())
    store(entity) = component

    component match
      case _ : WizardTypeComponent =>
        entitiesByType.getOrElseUpdate("wizard", mutable.Buffer()).append(entity)
      case _ : TrollTypeComponent =>
        entitiesByType.getOrElseUpdate("troll", mutable.Buffer()).append(entity)
      case _ : ProjectileTypeComponent =>
        entitiesByType.getOrElseUpdate("projectile", mutable.Buffer()).append(entity)
      case _ : Component =>

  def removeComponent[T <: Component: ClassTag](entityId: EntityId): Unit =
    components.get(implicitly[ClassTag[T]].runtimeClass).foreach(_.remove(entityId))

  def getComponent[T <: Component: ClassTag](entity: EntityId): Option[T] =
    components.get(implicitly[ClassTag[T]].runtimeClass).flatMap(_.get(entity).map(_.asInstanceOf[T]))

  def updateComponent[T <: Component: ClassTag](entity: EntityId, updateFn: T => T): Unit =
    val compClass = implicitly[ClassTag[T]].runtimeClass
    components.get(compClass).flatMap(_.get(entity)) match
      case Some(component) =>
        val updatedComponent = updateFn(component.asInstanceOf[T])
        removeComponent[T](entity)  
        addComponent(entity, updatedComponent)  
      case None =>
        throw new NoSuchElementException(
          s"Entity $entity does not have component of type ${compClass.getSimpleName}"
        )

  def hasComponent[T <: Component: ClassTag](entity: EntityId): Boolean =
    getComponent[T](entity).isDefined

  def getEntitiesWithComponent[T <: Component: ClassTag]: mutable.Buffer[EntityId] =
    components.get(implicitly[ClassTag[T]].runtimeClass).map(_.keys.toBuffer).getOrElse(mutable.Buffer.empty)

  def getEntitiesWithTwoComponents[A <: Component: ClassTag, B <: Component: ClassTag]: mutable.Buffer[EntityId] =
    val compAEntities = getEntitiesWithComponent[A]
    val compBEntities = getEntitiesWithComponent[B]
    compAEntities.intersect(compBEntities)

  def getEntitiesByType(entityType: String): mutable.Buffer[EntityId] =
    entitiesByType.getOrElse(entityType, mutable.Buffer.empty)

  def getAllEntities: mutable.Buffer[EntityId] = entities

  def getEntityAt(position: Position): Option[EntityId] =
    GridMapper.physicalToLogical(position) match
      case None => None
      case Some(targetGrid) =>
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

        val entitiesWithPos = getEntitiesWithComponent[PositionComponent].toList
        findEntityRecursive(entitiesWithPos)
        
  def getEntitiesAt(position: Position): Seq[EntityId] =
    GridMapper.physicalToLogical(position) match
      case None => Seq.empty
      case Some(targetGrid) =>
        getEntitiesWithComponent[PositionComponent].filter { entity =>
          getComponent[PositionComponent](entity) match
            case Some(posComp) =>
              GridMapper.physicalToLogical(posComp.position) match
                case Some(entityGrid) => entityGrid == targetGrid
                case _ => false
            case _ => false
        }.toSeq

  def hasWizardAt(position: Position): Boolean =
    getEntitiesAt(position).exists(entity => hasComponent[WizardTypeComponent](entity))

  def getEntityType(entity: EntityId): Option[EntityTypeComponent] =
    getComponent[WizardTypeComponent](entity)
      .orElse(getComponent[TrollTypeComponent](entity))
      .orElse(getComponent[ProjectileTypeComponent](entity))

  def clear(): Unit =
    entities.clear()
    components.clear()
    entitiesByType.clear()