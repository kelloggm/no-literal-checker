import org.checkerframework.checker.noliteral.qual.*;

class Conditionals {
    void test1(boolean b, int y) {
        int x;
        if (b) {
            x = 5;
        } else {
            x = y;
        }

        @MaybeConstant int z = x;

        // :: error: assignment.type.incompatible
        @NonConstant int w = x;
    }

    void test2(boolean b, int y) {
        int x = b ? 5 : y;

        @MaybeConstant int z = x;

        // :: error: assignment.type.incompatible
        @NonConstant int w = x;
    }
}