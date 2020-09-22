// There's plenty of bad crypto in this file, but nothing that the
// no literal checker should be concerned with.

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class SimpleCrypto {
  public void go() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    KeyGenerator keyGen = KeyGenerator.getInstance("DES");
    SecretKey key = keyGen.generateKey();
    Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
  }

  public static void main (String [] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    SimpleCrypto bc = new SimpleCrypto();
    bc.go();
  }
}
