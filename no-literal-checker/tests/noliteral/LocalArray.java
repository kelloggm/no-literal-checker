// Tests that a local array of constant strings
// has its type inferred correctly.

import org.checkerframework.checker.noliteral.qual.*;

class LocalArray {
    void test_constants() {
        String[] carr = new String[] {"foo", "bar", "baz"};
        @MaybeDerivedFromConstant String[] carr1 = new String[] {"foo", "bar", "baz"};
        // :: error: assignment.type.incompatible
        @NonConstant String[] carr2 = new String[] {"foo", "bar", "baz"};
        // :: error: assignment.type.incompatible
        @NonConstant String[] carr3 = carr;
    }

    void test_nonconstants(String s1, String s2, String s3) {
        String[] narr = new String[] {s1, s2, s3};
        @NonConstant String[] narr1 = new String[] {s1, s2, s3};
        // :: error: assignment.type.incompatible
        @NonConstant String[] narr2 = new String[] {s1, s2, "baz"};
        @NonConstant String[] narr3 = narr;
    }

    void test_assign(String[] arr1) {
        String[] parr = arr1;
    }
}