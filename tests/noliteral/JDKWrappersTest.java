// tests for handling of java.lang primitive wrappers

import org.checkerframework.checker.noliteral.qual.*;

class IntegerTest {
    void testIntToStringAndValueOf() {
        // :: error: assignment.type.incompatible
        @NonConstant String s = Integer.valueOf(5).toString();
    }

    void testParseInt() {
        // :: error: assignment.type.incompatible
        @NonConstant int x = Integer.parseInt("5");
    }

    void testByteToStringAndValueOf() {
        byte b = 0xa;
        // :: error: assignment.type.incompatible
        @NonConstant String s = Byte.valueOf(b).toString();
    }

    void testParseByte() {
        // :: error: assignment.type.incompatible
        @NonConstant byte x = Byte.parseByte("0xb", 4);
    }
}