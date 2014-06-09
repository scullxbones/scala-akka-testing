## Multi-JVM Testing

- Provides ability to coordinate actions across several nodes using `TestConductor`
- `MultiNodeConfig` is used to configure nodes and roles
- Spec must extend `MultiNodeSpec`
- `enterBarrier` method provides synchronization across nodes
- Supports simulation of failure conditions `blackhole`, `throttle`, `passThrough`
- `SbtMultiJvm` plugin is used to run multiple jvms on one or more machines