// Based on code found in the wild that issued a false positive
// because the defaulting rules were treating it as bytecode.
// The associated class file is also checked in, which is NOT
// a mistake. It's necessary to trigger the bug.

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import java.security.SecureRandom;

class WithBytecodeStillPresent {
  public static byte[] encryptByAES(String content, String password) {
    try {
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(128, new SecureRandom(password.getBytes()));
      SecretKey secretKey = kgen.generateKey();
      byte[] enCodeFormat = secretKey.getEncoded();
      SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, key);

      return cipher.doFinal(content.getBytes("utf-8"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}