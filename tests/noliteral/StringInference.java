import org.checkerframework.checker.noliteral.qual.*;

class StringInference {
    void test() {
        @MaybeConstant String x = "hello";

        // :: error: assignment.type.incompatible
        @NonConstant String y = "world";
    }
}