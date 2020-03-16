// tests for handling of java.lang primitive wrappers

import org.checkerframework.checker.noliteral.qual.*;

class JDKWrappersTest {

    private @MaybeDerivedFromConstant Byte b = 0xa;
    private @MaybeDerivedFromConstant Long l = 0l;
    private @MaybeDerivedFromConstant Integer i = 5;
    private @MaybeDerivedFromConstant Short s = 3;

    void testIntToStringAndValueOf() {
        // :: error: assignment.type.incompatible
        @NonConstant String s = Integer.valueOf(5).toString();
    }

    void testParseInt() {
        // :: error: assignment.type.incompatible
        @NonConstant int x = Integer.parseInt("5");
    }

    void testLongToStringAndValueOf() {
        // :: error: assignment.type.incompatible
        @NonConstant String s = Long.valueOf(5l).toString();
    }

    void testParseLong() {
        // :: error: assignment.type.incompatible
        @NonConstant long x = Long.parseLong("5l");
    }

    void testShortToStringAndValueOf() {
        short s1 = 5;
        // :: error: assignment.type.incompatible
        @NonConstant String s = Short.valueOf(s1).toString();
    }

    void testParseShort() {
        // :: error: assignment.type.incompatible
        @NonConstant short x = Short.parseShort("5");
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