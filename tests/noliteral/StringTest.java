// An example of code that was issuing a surprising warning in the
// NSI example.

class StringTest {
    void test(String info) {
        String[] splitInfo = info.split("/");
    }
}