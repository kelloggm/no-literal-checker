import org.checkerframework.checker.noliteral.qual.*;

import java.util.Random;

class IntRandom {
    void test() {
        @MaybeDerivedFromConstant int x = new Random().nextInt(4);

        @NonConstant int y = new Random().nextInt(5);
    }
}