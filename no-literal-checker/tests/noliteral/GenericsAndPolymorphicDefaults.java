// Tests a common case that came up in experiments:
// assuming polymorphic defaulting in bytecode, consider a program that interacts
// with a library by passing a constant String and receiving some object. That
// object is then placed into a List. If we use polymorphic defaulting for the resulting
// object, then even though it is an irrelevant type we'll get a false positive.

import javax.crypto.Cipher;
import java.util.List;
import java.util.ArrayList;

class GenericsAndPolymorphicDefaults {
  void test() throws Exception {
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    List<Cipher> myList = new ArrayList<Cipher>();
    myList.add(c);
  }

  void test2(Cipher c) {
    List<Cipher> myList = new ArrayList<Cipher>();
    myList.add(c);
  }
}