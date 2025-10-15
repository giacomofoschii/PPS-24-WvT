package it.unibo.pps.wvt.ecs.core

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

opaque type EntityId = Long

object EntityId:
  def apply(id: Long): EntityId = id

  def generate(): EntityId = EntityIdGenerator.generate()

  extension (id: EntityId)
    def value: Long      = id
    def toString: String = s"EntityId($id)"

case class EntityIdGenerator(nextId: Long = 1L):
  def generate(): (EntityIdGenerator, EntityId) =
    (EntityIdGenerator(nextId + 1), EntityId(nextId))

  def generateN(n: Int): (EntityIdGenerator, List[EntityId]) =
    (1 to n).foldLeft((this, List.empty[EntityId])): (acc, _) =>
      val (gen, ids)      = acc
      val (newGen, newId) = gen.generate()
      (newGen, ids :+ newId)

object EntityIdGenerator:
  private val globalGenerator: AtomicReference[EntityIdGenerator] =
    AtomicReference(EntityIdGenerator())

  @tailrec
  def generate(): EntityId =
    val currentGen      = globalGenerator.get()
    val (newGen, newId) = currentGen.generate()

    if globalGenerator.compareAndSet(currentGen, newGen) then
      newId
    else
      generate()

  def reset(): Unit =
    globalGenerator.set(EntityIdGenerator())

  def current: EntityIdGenerator =
    globalGenerator.get()
