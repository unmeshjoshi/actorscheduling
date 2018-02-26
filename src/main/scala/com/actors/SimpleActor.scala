package com.actors

import java.util.concurrent.TimeUnit

class SimpleActor extends Actor {
  override def receive = {
    case anyMessage@_ ⇒ {
      println(s"Received ${anyMessage}")
    }
  }
}

class AnotherActor extends Actor {
  override def receive = {
    case anyMessage@_ ⇒ {
      println(s"I am the second one ${anyMessage}")
    }
  }
}

object SimpleActorApp extends App {
  val system = new ActorSystem()

  val actorRef = system.actorOf(classOf[SimpleActor])
  for( a <- 1 to 1000) {
    actorRef ! s"Hello Actor${a}"
  }

  val anotherActorRef = system.actorOf(classOf[AnotherActor])
  for( a <- 1 to 1000) {
    anotherActorRef ! s"Hello Second Actor${a}"
  }

  system.awaitTermination(10, TimeUnit.SECONDS) //figure out why we need to do this.
}