package com.github.scullxbones.slides

import com.github.scullxbones.{WorkServiceComponent, ParentActor}
import akka.actor.{Props, ActorRef, PoisonPill}
import scala.concurrent.duration._

class ParentChildIntegrationSpec extends BaseActorSpec {

  import ParentActor._
  import org.mockito.Mockito._

  def withService[T <: WorkServiceComponent with ProvidesChildActorFactory](component: T)(testCode: (ActorRef, T) => Any) = {
    val underTest = system.actorOf(Props(new ParentActor(component.factory,1,3.seconds)),"parent-actor")
    try {
      testCode(underTest,component)
    } finally {
      underTest ! PoisonPill
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

}
