package com.github.scullxbones.scalatest

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import com.github.scullxbones.FizzBuzz
 
@RunWith(classOf[JUnitRunner])
class FizzBuzzScalaTest extends FlatSpec with Matchers {
  
  "A FizzBuzz processor" should "return 'FizzBuzz' from a multiple of three and five" in { 
      FizzBuzz(15) should be ("FizzBuzz") 
  }
    
  it should "return 'Fizz' from a multiple of three only" in { 
      FizzBuzz(12) should be ("Fizz") 
  }
    
  it should "return 'Buzz' from a multiple of five only" in { 
      FizzBuzz(10) should be ("Buzz") 
  }
    
  it should "return the stringified input from a non multiple of three or five" in { 
      FizzBuzz(11) should be ("11") 
  }
 
}