package com.actors

class ActorCell(clazz:Class[_], val dispatcher:Dispatcher) {
  private val mailBox = new MailBox(new UnboundedMessageQueue())
  mailBox.setActor(this)

  val receive: PartialFunction[Any, Unit] = clazz.newInstance().asInstanceOf[Actor].receive


  def mailbox = mailBox

  def receiveMessage(messageHandle: Envelope) = {
    receive(messageHandle.message)
  }

  def invoke(messageHandle: Envelope) = {
    receiveMessage(messageHandle)
  }


  def sendMessage(message:Any) = {
    dispatcher.dispatch(this, new Envelope(message))
  }
}
