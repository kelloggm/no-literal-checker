// Test that Random.nextInt() DOES NOT produce non-constant values.
// For cryptographic purposes, java.util.Random is NOT secure. Don't
// consider its return values non-constant! java.security.SecureRandom
// does produce non-constant random numbers.
//
// TODO: due to https://github.com/typetools/checker-framework/issues/3094,
// the results of Random's method are currently considered as secure as
// SecureRandom's!

import org.checkerframework.checker.noliteral.qual.*;

import java.util.Random;
import java.security.SecureRandom;

class IntRandom {
  void test() {
    @MaybeDerivedFromConstant int x = new Random().nextInt(4);

    @NonConstant int y = new Random().nextInt(5);
  }

  void testSecureRandom() {
    SecureRandom r = new SecureRandom();

    @MaybeDerivedFromConstant int x = r.nextInt(4);
    @NonConstant int y = r.nextInt(5);

    @MaybeDerivedFromConstant int x2 = new SecureRandom().nextInt(4);
    @NonConstant int y2 = new SecureRandom().nextInt(5);
  }
}
