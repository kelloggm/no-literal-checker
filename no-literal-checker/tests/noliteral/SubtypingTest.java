import org.checkerframework.checker.noliteral.qual.*;

// Test basic subtyping relationships for the NoLiteral Checker.
class SubtypeTest {
    void allSubtypingRelationships(@MaybeDerivedFromConstant int x, @NonConstant int y) {
        @MaybeDerivedFromConstant int a = x;
        @MaybeDerivedFromConstant int b = y;
        // :: error: assignment.type.incompatible
        @NonConstant int c = x; // expected error on this line
        @NonConstant int d = y;
    }
}
