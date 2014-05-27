package com.github.scullxbones

object FizzBuzz {
  
  def apply(x: Int) = (x % 3, x % 5, x) match {
    case(0, 0, _) => "FizzBuzz"
    case(0, _, _) => "Fizz"
    case(_, 0, _) => "Buzz"
    case _ => "" + x
  }
 
}