import org.checkerframework.checker.noliteral.qual.*;

class ByteInference {
    void test() {
        @MaybeConstant byte x = 0x00;

        // :: error: assignment.type.incompatible
        @NonConstant byte y = 0x00;
    }
}