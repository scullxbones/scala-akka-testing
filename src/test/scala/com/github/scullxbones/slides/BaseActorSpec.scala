package com.github.scullxbones.slides

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.FlatSpecLike
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.mock.MockitoSugar
import akka.testkit.ImplicitSender

abstract class BaseActorSpec extends TestKit(ActorSystem("unit-test")) 
	with FlatSpecLike 
	with Matchers 
	with MockitoSugar 
	with BeforeAndAfterAll 
	with ImplicitSender {
  
  override def afterAll = 
    TestKit.shutdownActorSystem(system)

}