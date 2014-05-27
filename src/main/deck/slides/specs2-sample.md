```scala
class EnterpriseDirectoryActionSpec extends Specification { def is = s2"""
  An enterprise directory action should provide enabled fields
    after a call to doDefault                                        ${fixture().e1}
    after a call to doSearchPrevious                                 ${fixture().e2}
    after a call to doSearchNext                                     ${fixture().e3}
    after a call to doExecuteSearch                                  ${fixture().e4}
    """

  case class fixture() extends Mockito {
    val fields = Seq("one", "two", "three")

    // some mock objects
    val directoryManager = mock[EnterpriseDirectoryManager]
    directoryManager.isLicenseValid returns true
    directoryManager.getEnabledFields returns fields.asJava
    directoryManager.shouldShowOrgChart("fred") returns true

    val user = mock[User]
    user.getName returns "fred"

    val sessionAccessor = mock[SessionAccessor]
    sessionAccessor.getSession returns mock[HttpSession]

    /** an instance of the class under test */
    val action = new EnterpriseDirectoryAction
    action.setEnterpriseDirectoryManager(directoryManager)
    action.setSessionAccessor(sessionAccessor)

    AuthenticatedUserThreadLocal.setUser(user)
    def e1 = { action.doDefault(); checkEnabledFields }
    def e2 = { action.doSearchPrevious(); checkEnabledFields }
    def e3 = { action.doSearchNext(); checkEnabledFields }

    def e4 = {
      action.setLastName("Bloggs")
      action.doExecuteSearch()
      checkEnabledFields
    }

    def checkEnabledFields = action.getEnabledFields.asScala must containTheSameElementsAs(fields)
  }
}
```
