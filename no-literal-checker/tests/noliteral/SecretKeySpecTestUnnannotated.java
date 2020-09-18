// A smoke test. This is an unnannotated program that should throw an error,
// because an annotation is needed.

import javax.crypto.spec.SecretKeySpec;

import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;

public class SecretKeySpecTestUnnannotated {

  private byte[] getKey() {
    // :: error: return.type.incompatible
    return new byte [] {0xa, 0xb};
  }

  public SecretKeySpec getKeySpec() {
    return new SecretKeySpec(getKey(), "AES");
  }
}
