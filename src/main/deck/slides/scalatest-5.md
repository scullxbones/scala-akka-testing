## Scalatest - Loan Fixture

<pre><code class="scala">
class TestSpec extends FlatSpec {
	def withStore(testCode: Store => Any) {
      val resource = Store.connect()
      try {
		testCode(resource)
      } finally {
        resource.dispose()
      }
    }

    "A loaned object" should "be returned" in withStore { store =>
       val result = store.doSomethingAnything()
       result should be (42)
    }
}
</code></pre>