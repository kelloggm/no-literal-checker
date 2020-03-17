// A smoke test.
// This should be the result of running WPI on this program if it is unannotated.

import javax.crypto.spec.SecretKeySpec;

import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;

public class SecretKeySpecTest {

    private @MaybeDerivedFromConstant byte[] getKey() {
        return new byte [] {0xa, 0xb};
    }

    public SecretKeySpec getKeySpec() {
        // :: error: argument.type.incompatible
        return new SecretKeySpec(getKey(), "AES");
    }
}
