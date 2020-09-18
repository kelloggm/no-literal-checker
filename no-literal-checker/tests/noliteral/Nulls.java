// Tests that null is never maybe constant. It should default to bottom.

import org.checkerframework.checker.noliteral.qual.*;

class Nulls {
  Object test() { return null; }
  @NonConstant Object test1() { return null; }
  @MaybeDerivedFromConstant Object test2() { return null; }
}
