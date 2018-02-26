package com.actors

trait ActorRef {
  def !(message:Any)
}

class LocalActorRef(clazz:Class[_], val dispatcher:Dispatcher) extends ActorRef {
  private val actorCell: ActorCell = new ActorCell(clazz, dispatcher)
  override def !(message: Any): Unit = actorCell.sendMessage(message)
}
