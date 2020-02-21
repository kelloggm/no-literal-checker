// tests for handling of java.lang.Integer

import org.checkerframework.checker.noliteral.qual.*;

class IntegerTest {
    void testToStringAndValueOf() {
        // :: error: assignment.type.incompatible
        @NonConstant String s = Integer.valueOf(5).toString();
    }

    void testParseInt() {
        // :: error: assignment.type.incompatible
        @NonConstant int x = Integer.parseInt("5");
    }
}