// This test checks that simple object operations behave as expected,
// including creating new objects, calling toString(), etc.

import org.checkerframework.checker.noliteral.qual.*;

class BasicObjects {
    @NonConstant Object createObject() {
        return new Object();
    }

    @NonConstant String callToString(Object o) {
        return o.toString();
    }
}