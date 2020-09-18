// Test of some code that issued an error during a larger-scale test.
// This has nothing to do with the security properties the no-literal
// checker ought to be checking, so it should not issue any errors here.
// The particular error that this test guards against is failing to
// correctly default the component types of arrays in bytecode, which will cause
// all the "write" calls with a string and a call to getBytes() to fail.

import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

class RandomAccessFileTest {
  void test(String encodedCert) {
    try (RandomAccessFile randomAccessFile = new RandomAccessFile("foo.txt", "rw")) {
      randomAccessFile.write("-----BEGIN CERTIFICATE-----\n".getBytes());
      int i = 0;
      for (; i<(encodedCert.length() - (encodedCert.length() % 64)); i+=64) {
        randomAccessFile.write(encodedCert.substring(i, i + 64).getBytes());
        randomAccessFile.write("\n".getBytes());
      }
      randomAccessFile.write(encodedCert.substring(i, encodedCert.length()).getBytes());
      randomAccessFile.write("\n".getBytes());
      randomAccessFile.write("-----END CERTIFICATE-----".getBytes());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
