// This test checks that simple object operations behave as expected,
// including creating new objects, calling toString(), etc.

import org.checkerframework.checker.noliteral.qual.*;

class BasicObjects {
    @NonConstant Object createObject() {
        return new Object();
    }

    @NonConstant String callToString(Object o) {
        // toString() is often constant-derived
        // :: error: return.type.incompatible
        return o.toString();
    }

    @NonConstant int callHashCode(Object o) {
        // hashCode's implementation also typically involves
        // doing math with constants
        // :: error: return.type.incompatible
        return o.hashCode();
    }
}
