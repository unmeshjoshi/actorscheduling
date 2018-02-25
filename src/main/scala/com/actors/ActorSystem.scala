package com.actors

import java.util.concurrent.ForkJoinPool

import scala.reflect.ClassTag

class ActorSystem(val dispatcher:Dispatcher = new Dispatcher(new ForkJoinPool(Runtime.getRuntime.availableProcessors))) {
  def actorOf[T <: Actor: ClassTag](clazz:Class[T]) = {
    new ActorCell(clazz, dispatcher)
  }
}
