package com.github.scullxbones

import akka.actor._
import scala.concurrent.duration._
import akka.event.LoggingReceive

object ParentActor {
  sealed trait Protocol
  case class Ack(id: String) extends Protocol
  case class Work(id: String) extends Protocol
  case class Success(id: String) extends Protocol
  case class Failure(id: String, exception: Throwable) extends Protocol
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
      handleWork(id, awaitingResponse)
      sender() ! Ack(id)
    case ChildFailure(id,exc) => awaitingResponse get id map (handleChildFailure(id,_,exc,awaitingResponse))
    case ChildSuccess(id) => awaitingResponse get id map (handleChildSuccess(id,_,awaitingResponse))
    case WorkTimeout(id) => awaitingResponse get id map (handleWorkTimeout(id,_,awaitingResponse))
  }

  def handleWork(id: String, awaitingResponse: Map[String,WorkTransaction]) {
    val child = childFactory(context)
    child ! DoWork(id)
    context.system.scheduler.scheduleOnce(timeout,self,WorkTimeout(id))
    context.become(running(awaitingResponse + (id -> WorkTransaction(sender(), child, maxRetries))))
  }

  def handleChildFailure(id: String, wt: WorkTransaction, exc: Throwable, awaitingResponse: Map[String,WorkTransaction]) {
    wt.replyTo ! Failure(id, exc)
    context.become(running(awaitingResponse - id))
  }

  def handleChildSuccess(id: String, wt: WorkTransaction, awaitingResponse: Map[String,WorkTransaction]) {
    wt.replyTo ! Success(id)
    context.become(running(awaitingResponse - id))
  }

  def handleWorkTimeout(id: String, wt: WorkTransaction, awaitingResponse: Map[String,WorkTransaction]) {
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

