package com.github.scullxbones.slides

import akka.testkit.{TestActorRef, TestProbe}
import akka.actor.PoisonPill
import com.github.scullxbones.{ParentActor, ServiceActor}
import scala.concurrent.duration._

class ServiceUnitSpec extends BaseActorSpec {

  val parentProbe = TestProbe()
  val clusterIntegrationProbe = TestProbe()

  def withService(testCode: TestActorRef[ServiceActor] => Any) {
    val sa = TestActorRef(new ServiceActor(_ => clusterIntegrationProbe.ref, 50.millis, 2))
    try {
      testCode(sa)
    } finally {
      sa ! PoisonPill
    }
  }

  import ServiceActor._

  "A service actor" should "send messages to the parent" in withService { ref =>
    ref ! Subscribe(parentProbe.ref)
    ref ! Work("unit-test")
    parentProbe.expectMsg(100.millis, ParentActor.Work("unit-test"))
    parentProbe.reply(ParentActor.Ack("unit-test"))
    parentProbe.reply(ParentActor.Success("unit-test"))
    expectMsg(100.millis,Success("unit-test"))
  }

  it should "retry on timeout" in withService { ref =>
    ref ! Subscribe(parentProbe.ref)
    ref ! Work("unit-test")
    parentProbe.expectMsg(250.millis, ParentActor.Work("unit-test"))
    parentProbe.expectMsg(250.millis, ParentActor.Work("unit-test"))
    parentProbe.reply(ParentActor.Ack("unit-test"))
    parentProbe.reply(ParentActor.Success("unit-test"))
    expectMsg(100.millis,Success("unit-test"))
  }

  it should "quit retrying on exhausted retries" in withService { ref =>
    ref ! Subscribe(parentProbe.ref)
    ref ! Work("unit-test")
    parentProbe.expectMsg(100.millis, ParentActor.Work("unit-test"))
    parentProbe.expectMsg(100.millis, ParentActor.Work("unit-test"))
    expectMsg(100.millis,NotAvailable)
  }

  it should "respond not available before a backend is available" in withService { ref =>
    ref ! Work("fail")
    expectMsg(100.millis,NotAvailable)
  }

  it should "respond not available if all backends that had subscribed unsubscribe" in withService { ref =>
    ref ! Subscribe(parentProbe.ref)
    ref ! Unsubscribe(parentProbe.ref)
    ref ! Work("fail")
    expectMsg(100.millis,NotAvailable)
  }

}
