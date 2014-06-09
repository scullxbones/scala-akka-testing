## Mocking vs Stubbing vs Spies

**Fakes** actually have working implementations, but usually take some shortcut which makes them not suitable for production

**Mocks** are pre-programmed with expectations which form a specification of the calls they are expected to receive

**Stubs** provide canned answers to calls made during the test, usually not responding at all to anything outside what's programmed in for the test

**Spies** record calls made for later verification.  Provides canned answers during test similar to a stub