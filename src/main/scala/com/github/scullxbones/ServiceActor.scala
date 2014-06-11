package com.github.scullxbones

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.Member
import akka.cluster.ClusterEvent.MemberUp
import akka.actor.Identify
import akka.actor.RootActorPath
import scala.util.Random
import scala.concurrent.duration._

object ServiceActor {
  sealed trait Protocol
  case class Work(id: String) extends Protocol
  case class Success(id: String) extends Protocol
  case class Failure(id: String) extends Protocol
  case object NotAvaialable extends Protocol
  
  object State {
    def apply(): State =
      State(Seq(),Map(),Map(),Map())
  }
  
  case class State(backends: Seq[ActorRef], 
		  		   replyTos: Map[String,ActorRef], 
		  		   inProcessJobs: Map[String,ActorRef], 
		  		   unackedJobs: Map[String,ActorRef]) {

    def nextBackend(jobId: String, replyTo: ActorRef): (ActorRef,State) = {
      val backend = backends.head
      (backend, copy(backends = backends.tail :+ backend, 
          			 unackedJobs = unackedJobs + (jobId -> backend),
          			 replyTos = replyTos + (jobId -> replyTo)))
    }
    
    def ackReceived(jobId: String): State =
      unackedJobs.get(jobId) map { ref =>
        copy(inProcessJobs = inProcessJobs + (jobId -> ref), unackedJobs = unackedJobs - jobId)  
      } getOrElse (this)
    
    def backendIdentified(backend: ActorRef): State =
      copy(backends = backends :+ backend)

    def backendTerminated(backend: ActorRef): State =
      copy(backends = backends.filterNot(_ == backend))
   
    def completedJob(jobId: String): (Option[ActorRef],State) = {
      val ref = inProcessJobs.get(jobId).orElse(unackedJobs.get(jobId)) 
      (ref, copy(inProcessJobs = inProcessJobs - jobId, unackedJobs = unackedJobs - jobId))
    }
  }
}

class ServiceActor extends Actor with ActorLogging {

  import ServiceActor._
  
  case class TimeoutAck(id: String, target: ActorRef, retries: Int)

  implicit val schedulerExecContext = context.dispatcher
  lazy val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
    context.become(running(State()))
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive = {
    case x => unhandled(x) 
  }

  def running(state: State): Receive = {
    case Work(id) =>
      val (target,nextState) = state.nextBackend(id, sender())
      target ! ParentActor.Work(id)
      context.system.scheduler.scheduleOnce(250.millis,self,TimeoutAck(id,target,3))
      context.become(running(nextState))
      
    case TimeoutAck(id,target,remainingRetries) if (remainingRetries > 1) =>
      target ! ParentActor.Work(id)
      context.system.scheduler.scheduleOnce(250.millis,self,TimeoutAck(id,target,remainingRetries - 1))
        
    case TimeoutAck(id,_,_) =>
      val (maybeRef,nextState) = state.completedJob(id)
      maybeRef map (_ ! NotAvaialable)
      context.become(running(nextState))
    
    case ParentActor.Ack(id) =>
      context.become(running(state.ackReceived(id)))
      
    case ParentActor.Success(id) =>
      val (maybeRef,nextState) = state.completedJob(id)
      maybeRef map (_ ! Success(id))
      context.become(running(nextState))
      
    case ParentActor.Failure(id, exc) =>
      val (maybeRef,nextState) = state.completedJob(id)
      maybeRef map (_ ! Failure(id))
      context.become(running(nextState))
      
    // Book keeping
    case MemberUp(m) if m.hasRole("backend") =>
      registerBackend(m)
    case MemberUp(m) =>
      log.info(s"Member up with roles: ${m.getRoles}")
    case ActorIdentity(_, Some(ref)) =>
      context watch ref
      context.become(running(state.backendIdentified(ref)))
    case ActorIdentity(address:String, None) =>
      log.warning(s"Could not resolve actor for address $address/user/parent")
    case Terminated(ref) =>
      log.warning(s"Remote ref $ref was terminated")
      context.become(running(state.backendTerminated(ref)))
  }

  def registerBackend(member: Member) {
    context.actorSelection(RootActorPath(member.address) / "user" / "parent") ! Identify(member.address.toString)
  }

}
