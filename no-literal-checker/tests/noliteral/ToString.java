// This file tests that toString() overrides behave as expected.

import org.checkerframework.checker.noliteral.qual.*;

public class ToString {
  @Override
  public @MaybeDerivedFromConstant String toString() {
    return "to string";
  }
}
