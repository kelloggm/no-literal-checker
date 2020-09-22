// From CryptoAPIBench. This is a test case for local array inference.

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class PredictableCryptographicKeyBBCase1 {
  public static void main(String [] args){
    String defaultKey = "SecDev2019";
    byte[] keyBytes = defaultKey.getBytes();
    // false positive on this line
    keyBytes = Arrays.copyOf(keyBytes, 16);
    // :: error: argument.type.incompatible
    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
  }
}
