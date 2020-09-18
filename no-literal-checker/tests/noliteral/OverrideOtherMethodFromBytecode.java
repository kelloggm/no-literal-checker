// tests that overrides from non-JDK methods defined in bytecode are allowed

import org.checkerframework.framework.test.ImmutableTestConfiguration;
import java.util.List;
import java.util.Map;
import java.io.File;

import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;

class OverrideOtherMethodFromBytecode extends ImmutableTestConfiguration {
  @Override
  public @MaybeDerivedFromConstant String toString() {
    return "hello world";
  }

  public OverrideOtherMethodFromBytecode(
      List<File> diagnosticFiles,
      List<File> testSourceFiles,
      List<String> processors,
      Map<String, String> options,
      boolean shouldEmitDebugInfo) {
    super(diagnosticFiles, testSourceFiles, processors, options, shouldEmitDebugInfo);
  }
}