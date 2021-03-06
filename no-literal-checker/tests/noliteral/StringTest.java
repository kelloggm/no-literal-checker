// An example of code that was issuing a surprising warning in the
// NSI example.

import org.checkerframework.checker.noliteral.qual.*;

class StringTest {
  void test(String info) {
    String[] splitInfo = info.split("/");
  }

  void splitConstant() {
    // :: error: assignment.type.incompatible
    @NonConstant String[] splits = "let's split this string".split(" ");
  }

  void equalsTest(String s) {
    boolean b = "".equals(s);
    boolean c = s.equals("");
    boolean d = "".equalsIgnoreCase(s);
    boolean e = s.equalsIgnoreCase("");
  }
}
