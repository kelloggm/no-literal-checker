package org.checkerframework.checker.noliteral;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.StubFiles;

/** The NoLiteral Checker enforces that literals don't go where they don't belong */
@StubFiles({"stubs/crypto.astub", "stubs/integer.astub", "stubs/byte.astub"})
public class NoLiteralChecker extends BaseTypeChecker {

  /**
   * In source code, the default type is {@code @NonConstant}. For bytecode, however, the checker
   * should only warn about sinks that were explicitly specified in stub files. Therefore, the
   * checker uses optimistic defaulting for bytecode, so that the default for parameters on classes
   * derived from bytecode is {@code @MaybeDerivedFromConstant}.
   */
  @Override
  public boolean useUncheckedCodeDefault(String kindOfCode) {
    if ("bytecode".equals(kindOfCode)) {
      return true;
    } else {
      return super.useUncheckedCodeDefault(kindOfCode);
    }
  }
}
