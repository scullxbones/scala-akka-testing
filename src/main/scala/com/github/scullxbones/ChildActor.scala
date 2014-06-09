package com.github.scullxbones

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive

object ChildActor {
  sealed trait Protocol
  case class DoWork(id: String) extends Protocol
  case class ChildFailure(id: String, exception: Exception) extends Protocol
  case class ChildSuccess(id: String) extends Protocol
}

trait ChildStack { self:  WorkServiceComponent =>
  class ChildActor extends Actor with ActorLogging {
    import ChildActor._

    def receive = LoggingReceive {
      case DoWork(id) =>
        try {
          workService.doWork(id)
          sender ! ChildSuccess(id)
        } catch {
          case t: RuntimeException =>
            sender ! ChildFailure(id,t)
        }
    }
  }
}

object ChildStackImpl
  extends ChildStack
  with WorkServiceComponentImpl