## Scalacheck

- Property-based testing, inspired by Haskell's `QuickCheck`
- Well supported by both `Scalatest` and `Specs2`
- Different style of testing, best illustrated by an example
- Less of a general tool, excellent for some use cases
	- Enforces rules, properties, invariants, conditions
	- Do not have to write test data, great for edge cases
- Sometimes difficult to express constraints as properties