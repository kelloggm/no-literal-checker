package org.checkerframework.checker.noliteral;

import javax.lang.model.element.ExecutableElement;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.TypeHierarchy;

public class NoLiteralVisitor extends BaseTypeVisitor {
  /**
   * @param checker the type-checker associated with this visitor (for callbacks to {@link
   *     TypeHierarchy#isSubtype})
   */
  public NoLiteralVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  // The following method override was stolen shamelessly from the Initialization Checker
  // of the Checker Framework. It is overridden to do nothing, because it normally enforces
  // that the "return type" of a constructor is top, which is wrong when the default type
  // in the hierarchy isn't top.
  @Override
  protected void checkConstructorResult(
      AnnotatedTypeMirror.AnnotatedExecutableType constructorType,
      ExecutableElement constructorElement) {
    // Nothing to check
  }
}
