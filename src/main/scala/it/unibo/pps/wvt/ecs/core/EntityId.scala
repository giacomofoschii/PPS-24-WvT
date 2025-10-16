package it.unibo.pps.wvt.ecs.core

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

opaque type EntityId = Long

/** Companion object for EntityId to provide utility methods. */
object EntityId:
  def apply(id: Long): EntityId = id

  /**  Generates a new unique EntityId using the EntityIdGenerator.
    *  @return A new unique EntityId.
    */
  def generate(): EntityId = EntityIdGenerator.generate()

  extension (id: EntityId)
    /**      Returns the underlying Long value of the EntityId.
      *      @return The Long value representing the EntityId.
      */
    def value: Long = id

    /**      Provides a string representation of the EntityId.
      *      @return A string in the format "EntityId(id)".
      */
    def toString: String = s"EntityId($id)"

/**  Case class to manage the generation of unique EntityIds.
  *  @param nextId The next available ID to be assigned.
  */
case class EntityIdGenerator(nextId: Long = 1L):
  /**    Generates a new EntityId and returns the updated generator.
    *    @return A tuple containing the updated EntityIdGenerator and the newly generated EntityId.
    */
  def generate(): (EntityIdGenerator, EntityId) =
    (EntityIdGenerator(nextId + 1), EntityId(nextId))

  /**    Generates a specified number of unique EntityIds.
    *    @param n The number of EntityIds to generate.
    *    @return A tuple containing the updated EntityIdGenerator and a list of newly generated EntityIds.
    */
  def generateN(n: Int): (EntityIdGenerator, List[EntityId]) =
    (1 to n).foldLeft((this, List.empty[EntityId])): (acc, _) =>
      val (gen, ids)      = acc
      val (newGen, newId) = gen.generate()
      (newGen, ids :+ newId)

/** Companion object for EntityIdGenerator to manage a global instance. */
object EntityIdGenerator:
  private val globalGenerator: AtomicReference[EntityIdGenerator] =
    AtomicReference(EntityIdGenerator())

  /**    Generates a new unique EntityId in a thread-safe manner.
    *    @return A new unique EntityId.
    */
  @tailrec
  def generate(): EntityId =
    val currentGen      = globalGenerator.get()
    val (newGen, newId) = currentGen.generate()

    if globalGenerator.compareAndSet(currentGen, newGen) then
      newId
    else
      generate()

  /** Resets the global EntityIdGenerator to its initial state. */
  def reset(): Unit =
    globalGenerator.set(EntityIdGenerator())

  /**    Retrieves the current state of the global EntityIdGenerator.
    *    @return The current EntityIdGenerator instance.
    */
  def current: EntityIdGenerator =
    globalGenerator.get()
