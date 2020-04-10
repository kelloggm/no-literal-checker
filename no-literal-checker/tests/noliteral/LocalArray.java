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
        // this is fine, because nef_arr is @MaybeConstant String[]
        nef_arr[1] = s;

        String[] nef_arr2 = new String[] {s, "bar", s};
        // this is fine, because nef_arr2 is @MaybeConstant String[]
        nef_arr2[1] = s;

        // nef_arr3 will forever have the type @MaybeConstant String[]
        String[] nef_arr3 = new String[] {"foo", "bar", "baz"};
        // even if it is re-assigned
        nef_arr3 = new String[] {s, s, s};
        // :: error: argument.type.incompatible
        requireNonConstantStringArray(nef_arr3);

        String[] nef_arr4 = new String[] {"foo", "bar", "baz"};
        // :: error: argument.type.incompatible
        test_assign(nef_arr4);

        // nef_arr5 should be @NonConstant String[]
        String[] nef_arr5 = new String[] {s, s, s};
        // :: error: assignment.type.incompatible
        nef_arr5[1] = "foo";

        String[] ef_arr = new String[] {"foo", "bar", "baz"};
        doNothing(ef_arr);
    }

    void test_assign(String[] arr1) {
        String[] parr = arr1;
    }

    @Pure
    void doNothing(@MaybeDerivedFromConstant String[] arr) { }

    void test_3d_array(String s) {
        String[][] two_d_array = new String[][] { {"foo", "bar"}, {"baz", "qux"}, {"thud", "razz"} };
        // :: error: assignment.type.incompatible
        @NonConstant String[][] nonconstant_array = two_d_array;

        String[][] two_d_array2 = new String[][] { {s, s}, {s, s}};
        // :: error: assignment.type.incompatible
        two_d_array2[0][0] = "foo";
    }

    void test_toCharArray(String s) {
        // toCharArray is polymorphic, so the RHS is @MaybeDerivedFromConstant char[]
        char[] array = "foobar".toCharArray();
        // :: error: argument.type.incompatible
        requireNonConstantCharArray(array);

        // s is nonConstant, so this array should be too
        char[] array2 = s.toCharArray();
        requireNonConstantCharArray(array2);

        // array3 should be @MaybeDerivedFromConstant char[]
        char[] array3 = myField;
        // :: error: argument.type.incompatible
        requireNonConstantCharArray(array);
    }

    @MaybeDerivedFromConstant char[] myField = {'f', 'o', 'o'};

    void requireNonConstantCharArray(@NonConstant char [] x) { }

    void requireNonConstantStringArray(@NonConstant String [] x) { }

    @MaybeDerivedFromConstant byte[] id(@MaybeDerivedFromConstant byte[] a) {
        return a;
    }

    void reassign() {
        String s = "foo";
        byte[] arr = s.getBytes();
        arr = id(arr);
    }
}
