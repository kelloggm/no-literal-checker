import org.checkerframework.checker.noliteral.qual.*;

// Test basic subtyping relationships for the NoLiteral Checker.
class SubtypeTest {
    void allSubtypingRelationships(@MaybeConstant int x, @NonConstant int y) {
        @MaybeConstant int a = x;
        @MaybeConstant int b = y;
        // :: error: assignment.type.incompatible
        @NonConstant int c = x; // expected error on this line
        @NonConstant int d = y;
    }
}
