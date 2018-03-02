package com.actors

import java.util.concurrent.{ConcurrentLinkedQueue, ForkJoinTask}

import sun.misc.Unsafe

import scala.annotation.tailrec

trait MessageQueue {
  def enqueue(receiver: ActorCell, handle: Envelope): Unit // NOTE: receiver is used only in two places, but cannot be removed
  def dequeue(): Envelope

  def numberOfMessages: Int

  def hasMessages: Boolean

  def cleanUp(owner: ActorCell, deadLetters: MessageQueue): Unit
}

class UnboundedMessageQueue extends ConcurrentLinkedQueue[Envelope] with MessageQueue {
  def queue = this

  override def enqueue(receiver: ActorCell, handle: Envelope): Unit = {
    queue add handle
//    println(numberOfMessages)
  }

  override def dequeue(): Envelope = queue.poll()

  def numberOfMessages = queue.size

  def hasMessages = !queue.isEmpty

  def cleanUp(owner: ActorCell, deadLetters: MessageQueue): Unit = {
    if (hasMessages) {
      var envelope = dequeue
      while (envelope ne null) {
        deadLetters.enqueue(owner, envelope)
        envelope = dequeue
      }
    }
  }
}

class MailBox(val messageQueue: MessageQueue) extends ForkJoinTask[Unit] {
  private var idle = true

  def isIdle = idle
  /**
    * Using synchronied to simplify things. In the real Akka actors code,
    * its highly optimized by using atomic compare and swap instruction
    */
  def setAsScheduled(): Boolean = {
    this.synchronized {
      if (idle) {
        idle = false
        true
      }
      else {
        false
      }
    }
  }

  def setAsIdle():Boolean = {
    this.synchronized {
      if (!idle) {
        idle = true
        true
      } else {
        false
      }
    }
  }

  def canBeScheduled() = {
    messageQueue.hasMessages
  }

  var actor: ActorCell = _

  def setActor(cell: ActorCell): Unit = actor = cell

  def dispatcher: Dispatcher = actor.dispatcher

  val shouldProcessMessage: Boolean = true

  /**
    * Enqueue messages in message queue
    * @param receiver
    * @param msg
    */
  def enqueue(receiver: ActorCell, msg: Envelope): Unit = messageQueue.enqueue(receiver, msg)

  /**
    * Try to dequeue the next message from this queue, return null failing that.
    */
  def dequeue(): Envelope = messageQueue.dequeue()

  @tailrec private final def processMailbox(
                                             left: Int = java.lang.Math.max(dispatcher.throughput, 1),
                                             deadlineNs: Long = if (dispatcher.isThroughputDeadlineTimeDefined == true) System.nanoTime + dispatcher.throughputDeadlineTime.toNanos else 0L): Unit =
    if (shouldProcessMessage) {
      val next = dequeue()
//      println(s"Dequeueing and processing messages ${next}")
      if (next ne null) {

//        println(s" There are ${messageQueue.numberOfMessages} messages")
//        println(" processing message " + next)

        actor invoke next

        if (Thread.interrupted())
          throw new InterruptedException("Interrupted while processing actor messages")

        if ((left > 1) && ((dispatcher.isThroughputDeadlineTimeDefined == false) || (System.nanoTime - deadlineNs) < 0)) //FIXME
          processMailbox(left - 1, deadlineNs)
      }
    }

  final def run(): Unit = {
    processMailbox()
  }

  override def exec() = {
    try {
      run()
    } finally {
      setAsIdle
      dispatcher.registerForExecution(this, false, false)
    }
    false //this is critical to tell forkjoinpool that the task is not completed.
  }

  override def getRawResult: Unit = {}

  override def setRawResult(value: Unit): Unit = {}
}

final case class Envelope(val message: Any /*, val sender: ActorCell*/)
