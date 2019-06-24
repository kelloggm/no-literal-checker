import org.checkerframework.checker.noliteral.qual.*;

class FloatInference {
    void test() {
        @MaybeConstant float x = 4.0f;

        // :: error: assignment.type.incompatible
        @NonConstant float y = 5.0f;
    }
}