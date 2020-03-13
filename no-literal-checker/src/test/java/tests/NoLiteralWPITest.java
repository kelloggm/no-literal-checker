package tests;

import java.io.File;
import java.util.List;
import org.checkerframework.checker.noliteral.NoLiteralChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/** Test runner that runs the No Literal Checker with WPI. */
public class NoLiteralWPITest extends CheckerFrameworkPerDirectoryTest {
  public NoLiteralWPITest(List<File> testFiles) {
    super(
        testFiles,
        NoLiteralChecker.class,
        "wpi",
        "-Anomsgtext",
        "-AstubDebug",
        "-nowarn",
        "-Ainfer=stubs",
        "-Astubs=stubs");
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {"wpi"};
  }
}