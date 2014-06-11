package com.github.scullxbones.slides

import akka.remote.testkit.{MultiNodeSpec, MultiNodeConfig}
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.Props
import com.github.scullxbones.{ParentActor, ServiceActor}


object MultiNodeConfiguration extends MultiNodeConfig {
  val frontend = role("frontend1")
  val backend1 = role("backend1")
  val backend2 = role("backend2")

  commonConfig(ConfigFactory.parseString(
    """
    |akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    |akka.remote.log-remote-lifecycle-events = off
    |akka.cluster.roles = [frontend,backend]
    |// router lookup config ...
    """.stripMargin))

//  nodeConfig(backend1,backend2)(ConfigFactory.parseString(
//    """
//    |akka.actor.deployment {
//    |  /parent {
//    |  }
//    |}
//    """.stripMargin))
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

  "A cluster" should "start up" in within(15 seconds) {
    Cluster(system).subscribe(testActor, classOf[MemberUp])
    expectMsgClass(classOf[CurrentClusterState])

    val firstAddress = node(frontend).address
    val secondAddress = node(backend1).address
    val thirdAddress = node(backend2).address

    Cluster(system) join secondAddress

    system.actorOf(Props[ParentActor], "parent")

    Cluster(system) join thirdAddress

    system.actorOf(Props[ParentActor], "parent")

    Cluster(system) join firstAddress

    system.actorOf(Props[ServiceActor], "service")

    receiveN(3).collect { case MemberUp(m) => m.address }.toSet should be(
      Set(firstAddress, secondAddress, thirdAddress))

    Cluster(system).unsubscribe(testActor)

    testConductor.enter("all-up")
  }
}
