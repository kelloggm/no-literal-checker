import org.checkerframework.checker.noliteral.qual.*;

import java.util.Random;

class Zeros {
    void test() {
        @NonConstant int x = new Random().nextInt(100);

        // :: error: assignment.type.incompatible
        @NonConstant int y = x & 0;
    }
}
