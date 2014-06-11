package com.github.scullxbones.slides

import scala.concurrent.duration._
import akka.testkit.TestActorRef
import akka.testkit.TestProbe
import com.github.scullxbones.{ChildActor, ParentActor}
import akka.actor.PoisonPill

class ParentUnitSpec extends BaseActorSpec {
  
  import ParentActor._
  import ChildActor._
  
  def withParentChild(testCode: (TestActorRef[ParentActor],TestProbe) => Any) = {
	val childProbe = TestProbe()
    val underTest = TestActorRef[ParentActor](new ParentActor(_ => childProbe.ref,1,50.millis),"parent-actor")
    try {
      testCode(underTest,childProbe)
    } finally {
      underTest ! PoisonPill
    }
  }

  "A parent actor" should "forward incoming messages to its child" in withParentChild { (underTest, childProbe) =>
    underTest ! Work("id")
    childProbe.expectMsg(1.seconds,DoWork("id"))
  }
  
  it should "respond to failure" in withParentChild { (underTest, childProbe) =>
    underTest ! Work("id")
    childProbe.expectMsg(1.seconds,DoWork("id"))
    val exc = new RuntimeException
    childProbe.send(underTest,ChildFailure("id",exc))
    expectMsg(Failure("id", exc))
  }
  
  it should "respond to success" in withParentChild { (underTest, childProbe) =>
    underTest ! Work("id")
    childProbe.expectMsg(1.seconds,DoWork("id"))
    childProbe.send(underTest,ChildSuccess("id"))
    expectMsg(Success("id"))
  }
  
  it should "retry on timeout and recover" in withParentChild { (underTest, childProbe) =>
    underTest ! Work("id")
    childProbe.expectMsg(1.seconds,DoWork("id"))
    childProbe.expectMsg(1.seconds,DoWork("id"))
    childProbe.send(underTest,ChildSuccess("id"))
    expectMsg(Success("id"))
  }
  
  it should "timeout" in withParentChild { (underTest, childProbe) => 
    underTest ! Work("id")
    childProbe.expectMsg(1.seconds,DoWork("id"))
    expectMsg(3.seconds,TryAgainLater)
  }
  
}