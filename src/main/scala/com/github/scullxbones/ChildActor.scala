package com.github.scullxbones

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ChildActor {
  sealed trait Protocol
  case class DoWork(id: String) extends Protocol
  case class ChildFailure(id: String, exception: Throwable) extends Protocol
  case class ChildSuccess(id: String) extends Protocol
}

trait ChildStack { self:  WorkServiceComponent =>
  class ChildActor extends Actor with ActorLogging {
    import ChildActor._

    // Would specify a separate ExecutionContext in real world
    implicit val ec = context.dispatcher

    def receive = LoggingReceive {
      case DoWork(id) =>
        val replyTo = sender() // Don't close over sender in onComplete!
        Future {
          workService.doWork(id)
        }.mapTo[Unit].onComplete {
          case Success(_) => replyTo ! ChildSuccess(id)
          case Failure(exc) => replyTo ! ChildFailure(id,exc)
        }
    }
  }
}

object ChildStackImpl
  extends ChildStack
  with WorkServiceComponentImpl