package com.github.scullxbones.slides

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.when
import org.scalatest.Matchers

trait FooService {
  def answerTheQuestionOfLife(): Int
}

class BarService(foo:FooService)  {
  def ask():Int = foo.answerTheQuestionOfLife()
}

class TestSpec extends FlatSpec with Matchers with MockitoSugar {
	trait BaseSetup {
      val mockFoo = mock[FooService]
      val underTest = new BarService(mockFoo)
    }

    trait MockSetup { self: BaseSetup =>
      val myValue = 42
      when(mockFoo.answerTheQuestionOfLife()).thenReturn(myValue)
	}

    "A foo" should "know the answer to life" in new BaseSetup with MockSetup {
	  underTest.ask() should be (42)
    }
}