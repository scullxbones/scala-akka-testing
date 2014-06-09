## Deep Dive - System Under Test

- The core system is a pair of actors, parent and child
  - Parent acts as supervisor to children
  - When new work comes in, a new child is created to perform it
  - Child "does work" by invoking a service call asynchronously
  - Successes & Failures from Child are relayed to Parent and on to client
  - Parent tracks work and performs a timeout/retry cycle
- For multi-jvm testing sharding of parent actors is added