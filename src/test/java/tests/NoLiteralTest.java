import java.io.File;
import java.util.List;
import org.checkerframework.checker.noliteral.NoLiteralChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test runner for tests of the NoLiteral Checker.
 *
 * <p>Tests appear as Java files in the {@code tests/noliteral} folder. To add a new test case,
 * create a Java file in that directory. The file contains "// ::" comments to indicate expected
 * errors and warnings; see
 * https://github.com/typetools/checker-framework/blob/master/checker/tests/README .
 */
public class NoLiteralTest extends CheckerFrameworkPerDirectoryTest {
  public NoLiteralTest(List<File> testFiles) {
    super(testFiles, NoLiteralChecker.class, "noliteral", "-Anomsgtext", "-AstubDebug", "-nowarn");
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {"noliteral"};
  }
}
