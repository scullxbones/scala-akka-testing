package com.github.scullxbones

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.Member
import akka.cluster.ClusterEvent.MemberUp
import akka.actor.Identify
import akka.actor.RootActorPath
import scala.util.Random

object ServiceActor {
  sealed trait Protocol
  case class Work(id: String)
}

class ServiceActor extends Actor with ActorLogging {

  import ServiceActor._

  lazy val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
    context.become(running(Seq()))
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive = {
    case _ => unhandled(_)
  }

  def running(backends: Seq[ActorRef]): Receive = {
    case Work(id) =>
      val idx = Random.nextInt(backends.size)
      backends(idx) ! ParentActor.Work(id)

    // Book keeping
    case MemberUp(m) if m.hasRole("backend") =>
      registerBackend(m)
    case MemberUp(m) =>
      log.info(s"Member up with roles: ${m.getRoles}")
    case ActorIdentity(_, Some(ref)) =>
      context watch ref
      context.become(running(backends :+ ref))
    case ActorIdentity(address:String, None) =>
      log.warning(s"Could not resolve actor for address $address/user/parent")
    case Terminated(ref) =>
      log.warning(s"Remote ref $ref was terminated")
      context.become(running(backends.filterNot(_ == ref)))
  }

  def registerBackend(member: Member) {
    context.actorSelection(RootActorPath(member.address) / "user" / "parent") ! Identify(member.address.toString)
  }

}
