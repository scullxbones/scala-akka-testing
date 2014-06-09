package com.github.scullxbones.slides

import com.github.scullxbones.{ChildStack, WorkServiceComponent}
import akka.actor.{Props, ActorRef, ActorRefFactory}
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.concurrent.duration._

trait ProvidesChildActorFactory { self: ChildStack with WorkServiceComponent =>

  val name: String

  def factory: ActorRefFactory => ActorRef =
    af => af.actorOf(Props(new this.ChildActor), name)

}

trait SuccessfulWorkService extends WorkServiceComponent {
  import org.scalatest.mock.MockitoSugar._

  val ws = mock[WorkService]

  def workService = ws

}

trait FailingWorkService extends WorkServiceComponent {
  import org.scalatest.mock.MockitoSugar._

  val ws = mock[WorkService]
  when(ws.doWork(anyString())).thenThrow(new RuntimeException("FAIL"))

  def workService = ws

}

trait HangingWorkService extends WorkServiceComponent {
  def workService = new WorkService {
    override def doWork(id: String): Unit = Thread.sleep(3.seconds.toMillis)
  }
}

object SuccessfulChildActor extends ChildStack with SuccessfulWorkService with ProvidesChildActorFactory {
  val name = "elijah-wood"
}

object FailingChildActor extends ChildStack with FailingWorkService with ProvidesChildActorFactory {
  val name = "macaulay-culkin"
}

object HungChildActor extends ChildStack with HangingWorkService with ProvidesChildActorFactory {
  val name = "jonathan-brandis"
}
