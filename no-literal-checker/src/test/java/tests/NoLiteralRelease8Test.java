package tests;

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
public class NoLiteralRelease8Test extends CheckerFrameworkPerDirectoryTest {
  public NoLiteralRelease8Test(List<File> testFiles) {
    super(
        testFiles,
        NoLiteralChecker.class,
        "noliteral",
        "-Anomsgtext",
        "-AstubDebug",
        "-Astubs=stubs",
        "-nowarn",
        "--release",
        "8");
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {"noliteral"};
  }
}
