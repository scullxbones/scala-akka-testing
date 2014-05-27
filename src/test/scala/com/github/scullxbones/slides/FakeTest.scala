package com.github.scullxbones.slides

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class RealConnection {
    def cleanUp() {}

	def somethingSlowOrExpensive(): String = 
	  "slow AND expensive"

	def methodUnderTest(): String =
	  try {
	    somethingSlowOrExpensive()
	  } finally {
	  	cleanUp()
	  }
}

class FakeySpec extends FlatSpec with Matchers {
	trait FakeOp { self: RealConnection =>
	  override def somethingSlowOrExpensive(): String =
		"fake"
	}

	"A connection" should "not be slow and expensive" in {
		val cxn = new RealConnection with FakeOp
		cxn.methodUnderTest() should be ("fake")
	}
}