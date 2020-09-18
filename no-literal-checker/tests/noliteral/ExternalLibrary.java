// test to check that defaulting for non-JDK external libraries is correct.
// Since org.checkerframework:framework-test is already on the compile
// classpath, it uses that library for testing.

import org.checkerframework.framework.test.TestUtilities;

import org.checkerframework.checker.noliteral.qual.*;

class ExternalLibrary {
  void test() {
    // no error should be issued - calls to external code w/ constants are allowed
    TestUtilities.getTestFile("baz");

    // Defaulting on this method doesn't work because of varargs. TODO: figure out why + fix
    // TestUtilities.findNestedJavaTestFiles("foo", "bar");

    // outputs should be @NonConstant
    @NonConstant String output = TestUtilities.diagnosticToString(null, true);
  }
}
