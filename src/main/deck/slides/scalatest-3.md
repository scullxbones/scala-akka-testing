## Scalatest - Assertion via Matchers Trait

<pre><code class="scala">
val result = ...
// Equality
result should be ("foo")
// Collection, String, etc length
result should have length 3
// Greater than, less then, etc.
result.length should be < 4
result.length should be > 2
// Fuzzy range
result.length should be (3 +- 1)
// Collection contains
List(1,2,3) should contain (3)
// Ands and ors
List(1,2,3) should (contain (2) and have length (3))
// Arbitrary property testing with symbols e.g. 'sym and properties
book should have (
  'title ("Programming in Scala"),
  'author (List("Odersky", "Spoon", "Venners")),
  'pubYear (2008)
)
// Intercepting exceptions
intercept[IndexOutOfBoundsException] {
  "foo".charAt(-1)
}
</code></pre>