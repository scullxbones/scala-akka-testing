## Integration testing & named actors

- When using named actors that are slow to stop, duplicate actor names exceptions can arise
- Can get around with a `TestProbe` deathwatch and `expectTerminated`
- This can make tests run longer than anonymous actors
  - Downside of anonymous actors is the logging is more difficult to grok

```scala
  val dwProbe = TestProbe()
  val underTest = system.actorOf(Props(new FooActor,"an-actor-named-foo")
  dwProbe watch underTest
  try {
    testCode(underTest,component)
  } finally {
    underTest ! PoisonPill
    dwProbe expectTerminated(underTest, 10.seconds)
  }
```