package com.github.scullxbones.slides

import com.github.scullxbones.ChildActor
import akka.testkit.TestActorRef
import akka.actor.PoisonPill
import scala.concurrent.duration._
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eql}
import akka.pattern.ask
import scala.util.Success
import akka.util.Timeout

class ChildUnitSpec extends BaseActorSpec {
  import ChildActor._

  def withSuccessfulChild(testCode: (TestActorRef[SuccessfulChildActor.ChildActor], SuccessfulWorkService) => Any) = {
    val underTest = TestActorRef[SuccessfulChildActor.ChildActor](new SuccessfulChildActor.ChildActor,"child-actor")
    try {
      testCode(underTest,SuccessfulChildActor)
    } finally {
      underTest ! PoisonPill
    }
  }

  def withFailedChild(testCode: (TestActorRef[FailingChildActor.ChildActor], FailingWorkService) => Any) = {
    val underTest = TestActorRef[FailingChildActor.ChildActor](new FailingChildActor.ChildActor,"child-actor")
    try {
      testCode(underTest,FailingChildActor)
    } finally {
      underTest ! PoisonPill
    }
  }

  implicit val to = Timeout(1.seconds)

  "A child actor" should "invoke the work service successfully" in withSuccessfulChild { (underTest,component) =>
    val future = underTest ? DoWork("foo")
    val Success(ChildSuccess(id: String)) = future.value.get
    id should be ("foo")
    verify(component.ws).doWork(eql("foo"))
  }

  it should "respond to the failing service with ChildFailure" in withFailedChild { (underTest,component) =>
    val future = underTest ? DoWork("foo")
    val Success(ChildFailure(id: String, ex: Throwable)) = future.value.get
    id should be ("foo")
    ex should not be null
    verify(component.ws).doWork(eql("foo"))
  }

}
