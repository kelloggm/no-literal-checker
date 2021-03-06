// A basic test case for the polymorphic qualifier.

import org.checkerframework.checker.noliteral.qual.*;

class SimplePoly {
  @PolyConstant int foo(@PolyConstant int x) {
    return x;
  }

  void testMaybe(@MaybeDerivedFromConstant int y) {
    @MaybeDerivedFromConstant int x = foo(y);
    // :: error: assignment.type.incompatible
    @NonConstant int z = foo(y);
  }

  void testNon(@NonConstant int y) {
    @MaybeDerivedFromConstant int x = foo(y);
    @NonConstant int z = foo(y);
  }

  @PolyConstant int[] bar(@PolyConstant int[] x) {
    return x;
  }

  void testPolyArrayM(@MaybeDerivedFromConstant int[] y) {
    @MaybeDerivedFromConstant int[] x = bar(y);
    // :: error: assignment.type.incompatible
    @NonConstant int[] z = bar(y);
  }

  void testPolyArrayN(@NonConstant int[] y) {
    @MaybeDerivedFromConstant int[] x = bar(y);
    @NonConstant int[] z = bar(y);
  }
}
