// a test that various ways for user-input to enter the program are treated as @NonConstant.

import org.checkerframework.checker.noliteral.qual.*;

class Inputs {
    void testSystem() {
        @NonConstant String variable = System.getenv("CHECKERFRAMEWORK");
        @NonConstant String property = System.getProperty("java.home");
        @NonConstant String property2 = System.getProperty("java.home", property);
        // :: error: assignment.type.incompatible
        @NonConstant String property3 = System.getProperty("java.home", "x");
    }
}