import org.checkerframework.checker.noliteral.qual.*;

class ByteArrayInference {
    void test() {
        @MaybeDerivedFromConstant byte[] x = {0x00, 0x0f};

        // :: error: assignment.type.incompatible :: error: array.initializer.type.incompatible
        @NonConstant byte[] y = {0x00, 0x0f};

        // :: error: assignment.type.incompatible :: error: array.initializer.type.incompatible
        byte @NonConstant [] z = {0x00, 0x0f};
    }
}