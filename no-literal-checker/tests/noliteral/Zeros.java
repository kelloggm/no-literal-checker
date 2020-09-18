// An example Mike wrote on the whiteboard. Checks that anding with zero
// produces a possibly-constant value.

import org.checkerframework.checker.noliteral.qual.*;

import java.security.SecureRandom;

class Zeros {
  void test() {
    @NonConstant int x = new SecureRandom().nextInt(100);

    // :: error: assignment.type.incompatible
    @NonConstant int y = x & 0;
  }
}
