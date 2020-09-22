// Tests that booleans are never maybe constant, because we aren't interested in checking
// for them, and they require a great many uninteresting annotations (booleans are always
// constant, effectively).

import org.checkerframework.checker.noliteral.qual.*;

class Booleans {
  void test(boolean b) { }

  void a() {
    test(false);
    test(true);

    @NonConstant boolean b = false;
    @NonConstant boolean a = true;
    @MaybeDerivedFromConstant boolean c = false;
    @MaybeDerivedFromConstant boolean d = true;
  }
}
