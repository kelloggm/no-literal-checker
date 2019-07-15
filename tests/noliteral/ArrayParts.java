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
        @MaybeConstant int[] key = {1, 2, 3};
        // :: error: argument.type.incompatible
        test(key);
    }
}