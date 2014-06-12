package com.github.scullxbones.slides

import akka.remote.testkit.{MultiNodeSpec, MultiNodeConfig}
import com.typesafe.config.ConfigFactory
import akka.cluster.{MemberStatus, Member, ClusterEvent, Cluster}
import akka.cluster.ClusterEvent._
import akka.actor._
import com.github.scullxbones.{ParentActor, ServiceActor}
import akka.testkit.{TestProbe, ImplicitSender}
import akka.remote.transport.ThrottlerTransportAdapter.Direction
import akka.remote.testconductor.RoleName
import akka.cluster.protobuf.msg.ClusterMessages.{MetricsGossipEnvelope, GossipStatus, GossipEnvelope}
import akka.cluster.ClusterEvent.MemberUp
import akka.remote.testconductor.RoleName
import akka.cluster.ClusterEvent.CurrentClusterState


object MultiNodeConfiguration extends MultiNodeConfig {
  val frontend = role("frontend1")
  val backend1 = role("backend1")
  val backend2 = role("backend2")

  commonConfig(ConfigFactory.parseString(
    """
    |akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    |akka.remote.log-remote-lifecycle-events = off
    |akka.log-dead-letters-during-shutdown = off
    |akka.stdout-loglevel=OFF
    |akka.loglevel = "INFO"
    |akka.actor.debug.receive = on
    |akka.cluster.metrics.collector-class = akka.cluster.JmxMetricsCollector
    """.stripMargin))

  nodeConfig(backend1,backend2)(ConfigFactory.parseString(
    """
      |akka.cluster.roles = [backend]
      |
    """.stripMargin))

  nodeConfig(frontend)(ConfigFactory.parseString(
    """
      |akka.cluster.roles = [frontend]
      |
    """.stripMargin))

}

class MultiNodeSlidesSpecMultiJvmNode1 extends MultiNodeSlidesSpec
class MultiNodeSlidesSpecMultiJvmNode2 extends MultiNodeSlidesSpec
class MultiNodeSlidesSpecMultiJvmNode3 extends MultiNodeSlidesSpec


abstract class MultiNodeSlidesSpec
  extends MultiNodeSpec(MultiNodeConfiguration)
  with STMultiNodeSpec 
  with ImplicitSender {

  import scala.concurrent.duration._
  import MultiNodeConfiguration._

  def initialParticipants = roles.size

  muteDeadLetters(
    classOf[GossipEnvelope],
    classOf[GossipStatus],
    classOf[MetricsGossipEnvelope],
    classOf[ClusterEvent.ClusterMetricsChanged],
    classOf[akka.actor.PoisonPill],
    classOf[akka.remote.transport.AssociationHandle.Disassociated],
    classOf[akka.remote.transport.ActorTransportAdapter.DisassociateUnderlying],
    classOf[akka.remote.transport.AssociationHandle.InboundPayload])(system)

  def assertUnreachable(subjects: RoleName*): Unit = {
    val expected = subjects.toSet.map { role:RoleName => node(role).address }
    awaitAssert(expected foreach { a => cluster.state.unreachable.map(_.address) should contain (a) })
  }

  val identifyProbe = TestProbe()
  def parent(roleName: RoleName): ActorRef = {
    system.actorSelection(node(roleName) / "user" / "parent").tell(Identify("parent"), identifyProbe.ref)
    identifyProbe.expectMsgType[ActorIdentity].ref.get
  }

  def service(roleName: RoleName): ActorRef = {
    system.actorSelection(node(roleName) / "user" / "service").tell(Identify("service"), identifyProbe.ref)
    identifyProbe.expectMsgType[ActorIdentity].ref.get
  }

  def simpleName(member: Member) = {
    member.address.toString
  }

  def infoMemberStates() {
    val members = Cluster.get(system).state.members
    info(s"Members: ${members.map(m => s"""${simpleName(m)}[${m.status}]""").mkString(", ")}")
  }

  val cluster = Cluster(system)

  "A cluster" should "start up" in within(15 seconds) {
    cluster.subscribe(testActor, classOf[MemberUp])
    expectMsgClass(classOf[CurrentClusterState])

    val firstAddress = node(frontend).address
    val secondAddress = node(backend1).address
    val thirdAddress = node(backend2).address

    cluster join firstAddress

    runOn(backend1) {
      system.actorOf(Props[ParentActor], "parent")
      enterBarrier("deployed")
    }

    runOn(backend2) {
      system.actorOf(Props[ParentActor], "parent")
      enterBarrier("deployed")
    }

    runOn(frontend) {
      enterBarrier("deployed")
      system.actorOf(Props[ServiceActor], "service")
    }

    receiveN(3).collect { case MemberUp(m) => m.address }.toSet should be(
      Set(firstAddress, secondAddress, thirdAddress))

    cluster.unsubscribe(testActor)

    enterBarrier("after-1st")
  }

  "A frontend service" should "talk to a backend service" in within(15 seconds) {
    runOn(frontend) {
      system.actorSelection("/user/service") ! ServiceActor.Work("foo")
      expectMsg(3.seconds, ServiceActor.Success("foo"))
    }

    enterBarrier("after-2nd")
  }

  it should "recognize when backend actors terminate" in within(15 seconds) {

    runOn(frontend) {
      val termProbe = TestProbe()

      val parent1 = parent(backend1)
      termProbe watch parent1
      parent1 ! PoisonPill
      termProbe expectTerminated parent1

      val parent2 = parent(backend2)
      termProbe watch parent2
      parent2 ! PoisonPill
      termProbe expectTerminated parent2

      val service1 = service(frontend)
      termProbe watch service1
      service1 ! ServiceActor.Work("#fail")
      expectMsg(3.seconds, ServiceActor.NotAvailable)
      service1 ! PoisonPill
      termProbe expectTerminated service1

      enterBarrier("all-down")
      enterBarrier("back-up")
      system.actorOf(Props[ServiceActor], "service")
    }

    runOn(backend1) {
      enterBarrier("all-down")
      system.actorOf(Props[ParentActor], "parent")
      enterBarrier("back-up")
    }

    runOn(backend2) {
      enterBarrier("all-down")
      system.actorOf(Props[ParentActor], "parent")
      enterBarrier("back-up")
    }

    enterBarrier("after-3rd")

  }

  it should "give up when backends crash" in within(15 seconds) {
    runOn(frontend) {
      testConductor.exit(backend1,0).await
      testConductor.exit(backend2,0).await
    }

    runOn(frontend) {
      infoMemberStates()

      system.actorSelection("/user/service") ! ServiceActor.Work("#fail")
      expectMsg(3.seconds, ServiceActor.NotAvailable)
    }

    enterBarrier("after-4th")
  }

}
