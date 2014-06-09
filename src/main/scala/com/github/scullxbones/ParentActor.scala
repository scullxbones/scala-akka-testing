package com.github.scullxbones

import akka.actor._
import scala.concurrent.duration._
import akka.event.LoggingReceive

object ParentActor {
  sealed trait Protocol
  case class Work(id: String) extends Protocol
  case class Success(id: String) extends Protocol
  case class Failure(exception: Exception) extends Protocol
  case object TryAgainLater extends Protocol
}

class ParentActor(childFactory: ActorRefFactory => ActorRef, maxRetries: Int = 3, timeout: FiniteDuration = 3.seconds) extends Actor with ActorLogging {
  import ParentActor._
  import ChildActor._

  def this() = this(fact => fact.actorOf(Props(new ChildStackImpl.ChildActor)))
  
  private implicit val schedulerEc = context.dispatcher
  
  // Internal Protocol
  private case class WorkTimeout(id: String)
  private case class WorkTransaction(replyTo: ActorRef, worker: ActorRef, remainingRetries: Int)
  
  override def preStart() = context.become(running(Map()))
  
  override def receive = LoggingReceive {
    case m => unhandled(m)
  }
  
  def running(awaitingResponse: Map[String,WorkTransaction]): Receive = LoggingReceive {
    case Work(id) =>
      val child = childFactory(context)
      child ! DoWork(id)
      context.system.scheduler.scheduleOnce(timeout,self,WorkTimeout(id))
      context.become(running(awaitingResponse + (id -> WorkTransaction(sender(), child, maxRetries))))
      
    case ChildFailure(id,exc) =>
      awaitingResponse get id map {
        wt => 
          wt.replyTo ! Failure(exc) 
          context.become(running(awaitingResponse - id))
      }
      
    case ChildSuccess(id) =>
      awaitingResponse get id map {
        wt => 
          wt.replyTo ! Success(id) 
          context.become(running(awaitingResponse - id))
      }
      
    case WorkTimeout(id) =>
      awaitingResponse get id map { wt =>
        if (wt.remainingRetries > 0) {
          wt.worker ! DoWork(id)
	      context.system.scheduler.scheduleOnce(timeout,self,WorkTimeout(id))
	      context.become(running(awaitingResponse + (id -> wt.copy(remainingRetries = wt.remainingRetries-1))))
        } else {
          wt.replyTo ! TryAgainLater
          context.become(running(awaitingResponse - id))
        }
          
      }
  }
  
}

