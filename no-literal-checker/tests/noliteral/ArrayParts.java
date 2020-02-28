// This test enforces that arrays with constant elements are
// treated as possibly constant, and that non-constant arrays
// cannot have constants assigned into any of their subparts.

import org.checkerframework.checker.noliteral.qual.*;

class ArrayParts {
    void test(int[] key) {
        // :: error: assignment.type.incompatible
        key[0] = 5;
    }

    void test2() {
        // :: error: assignment.type.incompatible :: error: array.initializer.type.incompatible
        int[] key = {1, 2, 3};
        test(key);
    }

    void test3() {
        @MaybeDerivedFromConstant int[] key = {1, 2, 3};
        // :: error: argument.type.incompatible
        test(key);
    }

    @MaybeDerivedFromConstant char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
}