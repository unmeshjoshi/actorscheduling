package com.actors

import java.io.{File, PrintWriter}
import java.util.concurrent.TimeUnit

class SimpleActor extends Actor {
  val writer = new  PrintWriter(new File("actor.log"))
  override def receive = {
    case anyMessage@_ ⇒ {
      writer.println(s"Received ${anyMessage}")
      writer.flush()
    }
  }
}


object SimpleActorApp extends App {
  val system = new ActorSystem()

  val actorRef = system.actorOf(classOf[SimpleActor])
  for( a <- 1 to 1000) {
    actorRef ! s"Hello Actor${a}"
  }

  system.dispatcher.executorService.awaitTermination(10, TimeUnit.MINUTES)
}