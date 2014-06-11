package com.github.scullxbones

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.Member
import akka.cluster.ClusterEvent.MemberUp
import akka.actor.Identify
import akka.actor.RootActorPath
import scala.concurrent.duration._
import akka.event.LoggingReceive

object ServiceActor {
  sealed trait Protocol
  case class Subscribe(ref: ActorRef) extends Protocol
  case class Unsubscribe(ref: ActorRef) extends Protocol
  case class Work(id: String) extends Protocol
  case class Success(id: String) extends Protocol
  case class Failure(id: String) extends Protocol
  case object NotAvailable extends Protocol
  
  object State {
    def apply(): State =
      State(Seq(),Map(),Map(),Map())
  }
  
  case class State(backends: Seq[ActorRef], 
		  		   replyTos: Map[String,ActorRef], 
		  		   inProcessJobs: Map[String,ActorRef], 
		  		   unackedJobs: Map[String,Cancellable]) {

    override def toString =
      Seq(s"backends: $backends",s"replyTos: $replyTos",s"inProcessJobs: $inProcessJobs",s"unackedJobs: $unackedJobs").mkString("\n")


    def nextBackend(jobId: String, replyTo: ActorRef): (Option[ActorRef],State) = {
      val backend = backends.headOption
      (backend, backend match {
        case Some(be) =>
          copy(backends = backends.tail :+ be,
            replyTos = replyTos + (jobId -> replyTo))
        case None =>
          this
      })
    }

    def withTimeout(jobId: String, schedulerJob: Cancellable): State =
      copy(unackedJobs = unackedJobs + (jobId -> schedulerJob) )
    
    def ackReceived(jobId: String, ref: ActorRef): (Option[Cancellable], State) =
      unackedJobs.get(jobId).map { job =>
        (Some(job),
          copy(inProcessJobs = inProcessJobs + (jobId -> ref),
               unackedJobs = unackedJobs - jobId))
      } getOrElse {
        (None,this)
      }
    
    def backendIdentified(backend: ActorRef): State =
      copy(backends = backends :+ backend)

    def backendTerminated(backend: ActorRef): State =
      copy(backends = backends.filterNot(_ == backend))
   
    def completedJob(jobId: String): (Option[ActorRef],State) = {
      val ref = replyTos.get(jobId)
      (ref, copy(inProcessJobs = inProcessJobs - jobId,
                 unackedJobs = unackedJobs - jobId,
                 replyTos = replyTos - jobId))
    }
  }
}

object ClusterIntegrationServiceActor {
  case class Introduction(serviceActor: ActorRef)
}

class ClusterIntegrationServiceActor extends Actor with ActorLogging {

  import ServiceActor._
  import ClusterIntegrationServiceActor._

  lazy val cluster = Cluster(context.system)

  private var watched = Set[ActorRef]()

  override def postStop() = {
    watched foreach context.unwatch
    cluster.unsubscribe(self)
  }

  def receive = {
    case Introduction(ref) =>
      cluster.subscribe(self, classOf[MemberUp])
      cluster.state.members.foreach(registerBackend)
      context.become(running(ref))
  }

  def running(serviceActor: ActorRef): Receive = {
    // Book keeping
    case MemberUp(m) if m.hasRole("backend") =>
      log.warning(s"Requesting parent actor for member $m")
      registerBackend(m)
    case MemberUp(m) =>
      log.info(s"Member up with roles: ${m.getRoles}")
    case ActorIdentity(_, Some(ref)) =>
      log.warning(s"Added backend member $ref")
      watched += ref
      context watch ref
      serviceActor ! Subscribe(ref)
    case ActorIdentity(address:String, None) =>
      log.warning(s"Could not resolve actor for address $address/user/parent")
    case Terminated(ref) =>
      log.warning(s"Remote ref $ref was terminated")
      serviceActor ! Unsubscribe(ref)
  }

  def registerBackend(member: Member) {
    context.actorSelection(parentPath(member.address)) ! Identify(member.address.toString)
  }

  def parentPath(address: Address):ActorPath =
    RootActorPath(address) / "user" / "parent"

}

class ServiceActor(clusterIntegrationFactory: ActorRefFactory => ActorRef, timeout: FiniteDuration = 250.millis, retries: Int = 3) extends Actor with ActorLogging {

  import ServiceActor._

  def this() = this(fact => fact.actorOf(Props[ClusterIntegrationServiceActor]))

  case class TimeoutAck(id: String, target: ActorRef, retries: Int)

  implicit val schedulerExecContext = context.dispatcher

  override def preStart(): Unit = {
    clusterIntegrationFactory(context) ! ClusterIntegrationServiceActor.Introduction(context.self)
    context.become(running(State()))
  }

  override def receive = {
    case x => unhandled(x) 
  }

  def withLogging(currentState: State, tag: String = "")(stateTransition: State => State) {
    log.debug(s"$tag:before\n$currentState")
    val nextState = stateTransition(currentState)
    log.debug(s"$tag:after\n$nextState")
    context.become(running(nextState))
  }

  def running(state: State): Receive = LoggingReceive {
    case Work(id) =>
      withLogging(state,"running") { st =>
        val (target,nextState) = state.nextBackend(id, sender())
        target.fold {
          sender() ! NotAvailable
          nextState
        } {
          ref =>
            ref ! ParentActor.Work(id)
            val job = context.system.scheduler.scheduleOnce(timeout, self, TimeoutAck(id, ref, retries))
            nextState.withTimeout(id, job)
        }

      }

    case TimeoutAck(id,target,remainingRetries) if remainingRetries > 1 =>
      target ! ParentActor.Work(id)
      context.system.scheduler.scheduleOnce(timeout,self,TimeoutAck(id,target,remainingRetries - 1))
        
    case TimeoutAck(id,_,_) =>
      withLogging(state,"timeout-exhausted") { st =>
        val (maybeRef,nextState) = state.completedJob(id)
        maybeRef map (_ ! NotAvailable)
        nextState
      }

    case ParentActor.Ack(id) =>
      withLogging(state,"ack") { st =>
        val (job,nextState) = st.ackReceived(id,sender())
        job map(_.cancel())
        nextState
      }

    case ParentActor.Success(id) =>
      withLogging(state,"success") { st =>
        val (maybeRef,nextState) = state.completedJob(id)
        maybeRef map (_ ! Success(id))
        nextState
      }

    case ParentActor.Failure(id, exc) =>
      withLogging(state,"failure") { st =>
        val (maybeRef,nextState) = state.completedJob(id)
        maybeRef map (_ ! Failure(id))
        nextState
      }

    case Subscribe(ref) =>
      withLogging(state,"subscribe")(_.backendIdentified(ref))

    case Unsubscribe(ref) =>
      withLogging(state,"unsubscribe")(_.backendTerminated(ref))
  }
}
