package com.github.scullxbones.specs2

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.github.scullxbones.FizzBuzz
 
@RunWith(classOf[JUnitRunner])
class FizzBuzzJUnitSpec extends org.specs2.mutable.Specification {
    
  "Multiples of both three and five" should {
    "print 'FizzBuzz'" in { FizzBuzz(15) must_== "FizzBuzz" }
  }
  "Multiples of three only" should {
    "print 'Fizz'" in { FizzBuzz(12) must_== "Fizz" }
  }
  "Multiples of five only" should {
    "print 'Buzz'" in { FizzBuzz(10) must_== "Buzz" }
  }
  "Non multiples of five or three" should {
    "print the number back" in { FizzBuzz(11) must_== "11" }
  }
}