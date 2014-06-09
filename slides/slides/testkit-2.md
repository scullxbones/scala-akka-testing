## Testkit - Design for testing with injection

- Parent-child interactions are important to test
  - Need to validate supervision behavior
  - Anything interesting in the protocol: Push/Pull, Timeouts, Retries, Error Handling
- `TestProbe` can stub out the other side of the interaction **if** the actor is designed for testing

Don't do:

```scala
class FooActor extends Actor {
	val child = context.actorOf(Props[BarActor]) // How to test this?
}
```

Consider using a higher-order function:

```scala
class FooActor(factory: ActorRefFactory => ActorRef) extends Actor {
	val child = factory(context)
}

// Production Code
val foo = system.actorOf(Props(new FooActor(_.actorOf(Props(new BarActor)))))

// Test Code
val probe = TestProbe()
val foo = TestActorRef(Props(new FooActor(_ => probe.ref)))
```