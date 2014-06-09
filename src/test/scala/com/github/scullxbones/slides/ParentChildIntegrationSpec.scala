package com.github.scullxbones.slides

import com.github.scullxbones.{WorkServiceComponent, ParentActor}
import akka.actor.{Props, ActorRef, PoisonPill}
import akka.pattern.gracefulStop
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.testkit.TestProbe

class ParentChildIntegrationSpec extends BaseActorSpec {

  import ParentActor._
  import org.mockito.Mockito._

  def withService[T <: WorkServiceComponent with ProvidesChildActorFactory](component: T)(testCode: (ActorRef, T) => Any) = {
    val dwProbe = TestProbe()
    val underTest = system.actorOf(Props(new ParentActor(component.factory,1,500.millis)),"parent-actor")
    dwProbe watch underTest
    try {
      testCode(underTest,component)
    } finally {
      underTest ! PoisonPill
      dwProbe expectTerminated(underTest, 10.seconds)
    }
  }

  "An integrated pair" should "work together" in withService(SuccessfulChildActor) { (underTest,component) =>
    underTest ! Work("id")
    expectMsg(500.millis,Success("id"))
    verify(component.ws).doWork("id")
  }

  it should "report failure when child fails" in withService(FailingChildActor) { (underTest, component) =>
    underTest ! Work("id")
    val failure = expectMsgType[Failure](500.millis)
    failure.exception.getClass should be (classOf[RuntimeException])
    verify(component.ws).doWork("id")
  }

  it should "let client know when child times out" in withService(HungChildActor) { (underTest, component) =>
    underTest ! Work("id")
    expectMsg(3.seconds,TryAgainLater)
  }

}
