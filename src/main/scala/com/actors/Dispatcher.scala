package com.actors

import java.util.concurrent.ForkJoinPool

import scala.concurrent.duration._

class Dispatcher(val executorService: ForkJoinPool) {
  val throughputDeadlineTime: Duration = Duration.Zero

  val isThroughputDeadlineTimeDefined = true

  val throughput: Int = 1

  def registerForExecution(mbox: MailBox, hasMessageHint: Boolean, hasSystemMessageHint: Boolean) = {
    if (mbox.canBeScheduled() && mbox.isIdle) {
      mbox.setScheduled() //FIXME need to do it atomically with compare and swap
      println(s"Calling execute as idle ${mbox.messageQueue.numberOfMessages}")
      executorService execute mbox
      println(s"${executorService.getQueuedTaskCount} tasks submitted to ${executorService.getActiveThreadCount} threads")

    }
  }

  def dispatch(receiver: ActorCell, invocation: Envelope) = {
    val mbox = receiver.mailbox
    mbox.enqueue(receiver, invocation)
    registerForExecution(mbox, true, false)
  }
}
