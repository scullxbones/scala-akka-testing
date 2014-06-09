## Mocking with Mockito

- Easy transition from using Mockito in Java
- Make sure to rename `eq` matcher due to clash with `Predef`:

```scala
import org.mockito.Matchers.{eq => eql}
```

```scala
class MockedSpec extends FlatSpec with MockitoSugar {
	trait Fixture {
      val foo = mock[FooService]
      when(foo.doSomethingStubbed()).thenReturn("STUB") // Stubbing
      when(foo.doSomething(eql("FOO"))).thenReturn("BAR")
      val bar = new BarService(foo)
    }

    "A bar service" should "collaborate with foo" in new Fixture {
      bar.doSomething() should be ("done")
      verify(foo).doSomething(eql("FOO")) // Mocking
    }
}
```