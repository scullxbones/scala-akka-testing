## Scalacheck

```scala
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
```