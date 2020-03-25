// Tests that a local array of constant strings
// has its type inferred correctly.

import org.checkerframework.checker.noliteral.qual.*;
import org.checkerframework.dataflow.qual.Pure;

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

    void test_non_effectively_final(String s) {
        String[] nef_arr = new String[] {"foo", "bar", "baz"};
        // :: error: assignment.type.incompatible
        nef_arr[1] = s;

        String[] nef_arr2 = new String[] {s, "bar", s};
        // :: error: assignment.type.incompatible
        nef_arr2[1] = s;

        // :: error: assignment.type.incompatible
        String[] nef_arr3 = new String[] {"foo", "bar", "baz"};
        nef_arr3 = new String[] {s, s, s};

        // :: error: assignment.type.incompatible
        String[] nef_arr4 = new String[] {"foo", "bar", "baz"};
        test_assign(nef_arr4);

        String[] ef_arr = new String[] {"foo", "bar", "baz"};
        doNothing(ef_arr);
    }

    void test_assign(String[] arr1) {
        String[] parr = arr1;
    }

    @Pure
    void doNothing(@MaybeDerivedFromConstant String[] arr) { }
}