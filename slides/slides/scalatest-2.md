## Scalatest - JUnit integration

- `JUnitRunner` allows JUnit to run tests, supports running in all IDEs
- Add following to `build.sbt` to generate test reports understood by Bamboo, Jenkins, etc.
<pre><code class="scala">
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports")
</code></pre>
