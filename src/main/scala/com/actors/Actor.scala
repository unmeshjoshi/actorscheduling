package com.actors

trait Actor {
  def receive: PartialFunction[Any, Unit]
}
