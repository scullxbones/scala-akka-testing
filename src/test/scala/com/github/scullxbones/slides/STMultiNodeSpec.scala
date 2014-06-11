package com.github.scullxbones.slides

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpecLike}
import org.scalatest.mock.MockitoSugar
import akka.testkit.ImplicitSender

trait STMultiNodeSpec
  extends MultiNodeSpecCallbacks
  with FlatSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}
