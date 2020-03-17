// test that the inferred types of various kinds of constants are correct

import org.checkerframework.checker.noliteral.qual.*;

class Constants {
    void testByte() {
        @MaybeDerivedFromConstant byte x = 0x00;

        // :: error: assignment.type.incompatible
        @NonConstant byte y = 0x00;
    }

    void testByteArray() {
        @MaybeDerivedFromConstant byte[] x = {0x00, 0x0f};

        // :: error: assignment.type.incompatible :: error: array.initializer.type.incompatible
        @NonConstant byte[] y = {0x00, 0x0f};

        // :: error: assignment.type.incompatible :: error: array.initializer.type.incompatible
        byte @NonConstant [] z = {0x00, 0x0f};
    }

    void testDouble() {
        @MaybeDerivedFromConstant double x = 4.0;

        // :: error: assignment.type.incompatible
        @NonConstant double y = 5.0;
    }

    void testFloat() {
        @MaybeDerivedFromConstant float x = 4.0f;

        // :: error: assignment.type.incompatible
        @NonConstant float y = 5.0f;
    }

    void testInt() {
        @MaybeDerivedFromConstant int x = 4;

        // :: error: assignment.type.incompatible
        @NonConstant int y = 5;
    }

    void testLong() {
        @MaybeDerivedFromConstant long x = 4l;

        // :: error: assignment.type.incompatible
        @NonConstant long y = 5l;
    }

    void testString() {
        @MaybeDerivedFromConstant String x = "hello";

        // :: error: assignment.type.incompatible
        @NonConstant String y = "world";
    }
}
