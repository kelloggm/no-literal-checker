import org.checkerframework.checker.noliteral.qual.*;

class IntInference {
    void test() {
        @MaybeConstant int x = 4;

        // :: error: assignment.type.incompatible
        @NonConstant int y = 5;
    }
}