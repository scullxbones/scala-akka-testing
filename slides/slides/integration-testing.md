## Integration testing & `TestProbe`

- Use system.actorOf to start actor subsystem, test as blocks
- Use `TestProbe` at boundaries to confirm behavior and assert interactions
- Use `ImplicitSender` to assert interactions with the entrypoint actors
- False negatives (test failures) can arise with `expectMsg` timeouts and slow-running environments
  - Can be an iterative process
  - Can use the `.dilated` implicit on `Duration`; Scaled by configuration item `akka.test.timefactor`
- Take care with using `CallingThreadDispatcher` and `TestProbe`, deadlocks are a possibility
- Logging can be used to help understand the behavior in case you're stumped

```
akka {
  loglevel = "DEBUG"
  actor {
    debug {
      receive = on
      autoreceive = on
      lifecycle = on
    }
  }
}
```