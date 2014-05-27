package com.github.scullxbones.scalacheck

import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalatest.Matchers
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import com.github.scullxbones.FizzBuzz
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FizzBuzzSpec extends PropSpec with PropertyChecks with Matchers {
  
  val ints = Gen.choose(Integer.MIN_VALUE,Integer.MAX_VALUE)
  
  property("A FizzBuzz processor should play fizzbuzz correctly") {
   forAll(ints) { (n: Int) =>
    if(n % 15 == 0) FizzBuzz(n) should be("FizzBuzz") 
    else if(n % 3 ==0) FizzBuzz(n) should be("Fizz")
    else if(n % 5 ==0) FizzBuzz(n) should be("Buzz")
    else "" + n == FizzBuzz(n) 
   }
  }
  
}