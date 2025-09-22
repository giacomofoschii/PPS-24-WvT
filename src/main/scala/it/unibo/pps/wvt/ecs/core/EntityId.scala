package it.unibo.pps.wvt.ecs.core

case class EntityId(id: Long) extends AnyVal {
  override def toString: String = s"EntityId($id)"
}

object EntityId {
  private var counter: Long = 0L

  def generate(): EntityId =
    counter += 1
    EntityId(counter)
}