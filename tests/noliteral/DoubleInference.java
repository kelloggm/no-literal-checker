import org.checkerframework.checker.noliteral.qual.*;

class DoubleInference {
    void test() {
        @MaybeDerivedFromConstant double x = 4.0;

        // :: error: assignment.type.incompatible
        @NonConstant double y = 5.0;
    }
}