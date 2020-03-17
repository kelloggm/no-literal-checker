// tests that unspecified parts of the JDK are treated as unannotated code

class OtherJDKLibraries {
    static void test() throws Exception {
        Thread.sleep(5);
    }
}
