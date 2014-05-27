## Scalatest - Styles

[Selecting a style](http://scalatest.org/user_guide/selecting_a_style)

- `FunSuite`: closest to JUnit; not very BDD
- `FlatSpec`: transition from JUnit to BDD, enforces BDD style
- `FunSpec`: closer to Ruby's `RSpec`
- `WordSpec`: similarities to `Specs2` specifications
- `FreeSpec`: "build your own BDD style" approach
- `Spec`: less compilation leads to faster build-test cycles; needs a significant number of tests to impact
- `PropSpec`: Property test style - think `ScalaCheck`
- `FeatureSpec`: Acceptance test style, similarities to Cucumber