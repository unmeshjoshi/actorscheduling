package com.actors

import java.util.concurrent.{ForkJoinPool, TimeUnit}

import scala.reflect.ClassTag

class ActorSystem(val dispatcher:Dispatcher = new Dispatcher(new ForkJoinPool(Runtime.getRuntime.availableProcessors))) {
  def awaitTermination(value: Int, unit: TimeUnit) = {
    dispatcher.executorService.awaitTermination(value, unit)
  }

  def actorOf[T <: Actor: ClassTag](clazz:Class[T]) = {
    new LocalActorRef(clazz, dispatcher)
  }
}
