// A false positive test case from CryptoAPIBench.
// I think this test fails because of some interaction
// between polymorphism and local array type inference,
// but I'm not sure. Only the first method ("allTogether")
// actually issues a false positive; all the modified
// versions in the rest of the test pass.
//
// @skip-test

import org.checkerframework.checker.noliteral.qual.*;
import java.util.stream.IntStream;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

class KeystoreCorrectExample {
    URL cacerts;
    public void allTogether() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        String type = "JKS";
        SecureRandom random = new SecureRandom();
        String password = String.valueOf(random.ints());
        byte [] keyBytes = password.getBytes("UTF-8");

        KeyStore ks = KeyStore.getInstance(type);
        cacerts = new URL("https://www.google.com");
        // false positive here
        ks.load(cacerts.openStream(), new String(keyBytes).toCharArray());
    }

    public void allTogether2() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        String type = "JKS";
        SecureRandom random = new SecureRandom();
        @NonConstant String password = String.valueOf(random.ints());
        @NonConstant byte @NonConstant [] keyBytes = password.getBytes("UTF-8");

        KeyStore ks = KeyStore.getInstance(type);
        cacerts = new URL("https://www.google.com");
        @NonConstant char @NonConstant [] chars = new String(keyBytes).toCharArray();
        ks.load(cacerts.openStream(), chars);
    }

    void test_randomInts() {
        @NonConstant IntStream ints = new SecureRandom().ints();
    }

    void test_valueOf(@NonConstant IntStream ints) {
        @NonConstant String password = String.valueOf(ints);
    }

    void test_getBytes(@NonConstant String password) throws Exception {
        @NonConstant byte @NonConstant [] keyBytes = password.getBytes("UTF-8");
    }

    void test_getBytes2(@NonConstant String password) throws Exception {
        byte [] keyBytes = password.getBytes("UTF-8");
    }

    void test_newString(@NonConstant byte @NonConstant [] keyBytes) {
        @NonConstant String result = new String(keyBytes);
    }

    void test_toCharArray(@NonConstant String result) {
        @NonConstant char @NonConstant [] chars = result.toCharArray();
    }

    void test_newString_toCharArray(@NonConstant byte @NonConstant [] keyBytes) {
        @NonConstant char @NonConstant [] chars = new String(keyBytes).toCharArray();
    }

    void test_load(@NonConstant char @NonConstant [] chars, KeyStore ks) throws Exception {
        ks.load(null, chars);
    }
}