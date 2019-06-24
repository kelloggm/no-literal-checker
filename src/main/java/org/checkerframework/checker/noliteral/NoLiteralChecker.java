package org.checkerframework.checker.noliteral;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.SuppressWarningsKeys;

/** The NoLiteral Checker enforces that literals don't go where they don't belong */
@SuppressWarningsKeys("noliteral")
public class NoLiteralChecker extends BaseTypeChecker {

  /**
   * Overridden so that unchecked code defaults are always used for bytecode, because we only want
   * to enforce @NonConstant if a stub file is present.
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
