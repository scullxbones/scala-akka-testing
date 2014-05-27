```scala
class EnterpriseDirectoryActionFlatSpec extends FlatSpec with ShouldMatchers with MockitoSugar {
  trait Fixture {
    val fields = Seq("one", "two", "three")

    // some mock objects
    val directoryManager = mock[EnterpriseDirectoryManager]
    when(directoryManager.isLicenseValid).thenReturn(true)
    when(directoryManager.getEnabledFields).thenReturn(fields.asJava)
    when(directoryManager.shouldShowOrgChart("fred")).thenReturn(true)

    val sessionAccessor = mock[SessionAccessor]
    when(sessionAccessor.getSession).thenReturn(mock[HttpSession])

    val action = new EnterpriseDirectoryAction(directoryManager, sessionAccessor)
  }

  "An enterprise directory action" should "provide enabled fields after a call to doDefault" in new Fixture {
    action.doDefault()
    action.getEnabledFields should be (f.fields)
  }

  it should "provide enabled fields after a call to doSearchPrevious" in new Fixture {
    action.doSearchPrevious()
    action.getEnabledFields should be (f.fields)
  }

  it should "provide enabled fields after a call to doSearchNext" in new Fixture {
    action.doSearchNext()
    action.getEnabledFields should be (f.fields)
  }

  it should "provide enabled fields after a call to doExecuteSearch" in new Fixture {
    action.setLastName("Bloggs")
    action.doExecuteSearch()
    action.getEnabledFields should be (f.fields)
  }
}
```