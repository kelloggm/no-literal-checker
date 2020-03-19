// Tests that no spurious errors are issued when annotations
// with constant string arguments are present in source code.
// Tests both declaration and type annotations.

import org.checkerframework.checker.index.qual.*;

class AnnotationWithArgTest {

    @HasSubsequence(subsequence = "this", from = "this.start", to = "this.end")
    int [] array;

    @IndexFor("array") int start;

    void test(@IndexOrHigh("array") int end) { }
}
