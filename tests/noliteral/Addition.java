// This test checks that expressions with constant subexpressions are
// possibly constant.

import org.checkerframework.checker.noliteral.qual.*;

class Addition {
    void test12() {
        @MaybeConstant int x = 1 + 2;

        // :: error: assignment.type.incompatible
        @NonConstant int z = 1 + 2;
    }

    void test1y(int y) {
        @MaybeConstant int x = 1 + y;

        // :: error: assignment.type.incompatible
        @NonConstant int z = 1 + y;
    }

    void test1concaty(int y) {
        @MaybeConstant String x = "1" + y;

        // :: error: assignment.type.incompatible
        @NonConstant String z = "1" + y;
    }
}