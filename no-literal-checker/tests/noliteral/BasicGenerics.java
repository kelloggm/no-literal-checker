// This test checks that simple uses of generics behave
// as expected, and do not issue false positive warnings.

import org.checkerframework.checker.noliteral.qual.*;
import java.util.*;

@SuppressWarnings("unchecked")
class BasicGenerics {
    void test() {
        List<String> list = new ArrayList();
    }

    void test1() {
        List<?> list = new ArrayList();
    }

    void test2() {
        List<Object[]> list = new ArrayList();
    }

    void testConstantList() {
        List<String> list = new ArrayList();
        list.add("foo");
    }

    void testListFromBytecode() {
        List<String> list0 = Collections.emptyList();
    }

    void testListFromUnannotatedMethod() {
        List<String> list = getList();
    }

    List<String> getList() {
        return Collections.emptyList();
    }
}