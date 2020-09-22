package org.checkerframework.checker.noliteral;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.javacutil.ElementUtils;

/**
 * This visitor has a lot of code in it that permits this type system to operate as expected with
 * the bottom type as its default.
 */
public class NoLiteralVisitor extends BaseTypeVisitor<NoLiteralAnnotatedTypeFactory> {
  /**
   * @param checker the type-checker associated with this visitor (for callbacks to {@link
   *     TypeHierarchy#isSubtype})
   */
  public NoLiteralVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  /**
   * Change the default for exception parameter lower bounds to NonConstant, to prevent false
   * positives. I think it might be a bug in the Checker Framework that these locations are always
   * defaulted to top - that doesn't make sense for checkers that use bottom as the default.
   *
   * @return a set containing only the @NonConstant annotation
   */
  @Override
  protected Set<? extends AnnotationMirror> getExceptionParameterLowerBoundAnnotations() {
    return Collections.singleton(atypeFactory.getNonConstant());
  }

  /**
   * The Checker Framework's default implementation of this method defers to {@code
   * #getExceptionParameterLowerBoundAnnotations}. That is a bug; this method should always return
   * the set containing top, regardless of what that method returns. This implementation does so.
   *
   * @return a set containing only the @MaybeDerivedFromConstant annotation
   */
  @Override
  protected Set<? extends AnnotationMirror> getThrowUpperBoundAnnotations() {
    return Collections.singleton(atypeFactory.getMaybeConstant());
  }

  /**
   * All annotation arguments must be literals, so there is no need to check them. See
   * https://github.com/typetools/checker-framework/issues/3178 for an explanation of why this is
   * necessary to avoid false positives.
   */
  @Override
  public Void visitAnnotation(AnnotationTree node, Void p) {
    return null;
  }

  /**
   * Skip the standard subtyping check on receivers on all method calls. Constants can't have
   * methods called on them, so it doesn't matter.
   */
  @Override
  protected boolean skipReceiverSubtypeCheck(
      final MethodInvocationTree node,
      final AnnotatedTypeMirror methodDefinitionReceiver,
      final AnnotatedTypeMirror methodCallReceiver) {
    return true;
  }

  @Override
  protected void checkConstructorResult(
      AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {
    // Skip this and do nothing. Normally, this method will issue a warning when
    // the type of a class defaults to something other than top. That's fine
    // for this checker, because we don't expect user-written classes to take
    // on values other than @NonConstant.
  }

  /**
   * The standard override checker is replaced so that no override.param.invalid errors are issued
   * when overriding methods defined in bytecode (because bytecode uses a different, incompatible
   * defaulting scheme).
   */
  @Override
  protected OverrideChecker createOverrideChecker(
      Tree overriderTree,
      AnnotatedExecutableType overrider,
      AnnotatedTypeMirror overridingType,
      AnnotatedTypeMirror overridingReturnType,
      AnnotatedExecutableType overridden,
      AnnotatedTypeMirror.AnnotatedDeclaredType overriddenType,
      AnnotatedTypeMirror overriddenReturnType) {
    return new NoLiteralOverrideChecker(
        overriderTree,
        overrider,
        overridingType,
        overridingReturnType,
        overridden,
        overriddenType,
        overriddenReturnType);
  }

  private class NoLiteralOverrideChecker extends OverrideChecker {
    /**
     * Create an OverrideChecker.
     *
     * <p>Notice that the return types are passed in separately. This is to support some types of
     * method references where the overrider's return type is not the appropriate type to check.
     *
     * @param overriderTree the AST node of the overriding method or method reference
     * @param overrider the type of the overriding method
     * @param overridingType the type enclosing the overrider method, usually an
     *     AnnotatedDeclaredType; for Method References may be something else
     * @param overridingReturnType the return type of the overriding method
     * @param overridden the type of the overridden method
     * @param overriddenType the declared type enclosing the overridden method
     * @param overriddenReturnType the return type of the overridden method
     */
    public NoLiteralOverrideChecker(
        Tree overriderTree,
        AnnotatedExecutableType overrider,
        AnnotatedTypeMirror overridingType,
        AnnotatedTypeMirror overridingReturnType,
        AnnotatedExecutableType overridden,
        AnnotatedTypeMirror.AnnotatedDeclaredType overriddenType,
        AnnotatedTypeMirror overriddenReturnType) {
      super(
          overriderTree,
          overrider,
          overridingType,
          overridingReturnType,
          overridden,
          overriddenType,
          overriddenReturnType);
    }

    @Override
    public boolean checkOverride() {
      // Don't issue override errors if the overridden method was defined
      // in bytecode, which uses a different defaulting scheme.
      if (!ElementUtils.isElementFromSourceCode(overridden.getElement())) {
        List<AnnotatedTypeMirror> paramTypes = overridden.getParameterTypes();
        for (int i = 0; i < paramTypes.size(); i++) {
          AnnotatedTypeMirror paramType = paramTypes.get(i);
          paramType.replaceAnnotation(atypeFactory.getNonConstant());
        }
        overriddenReturnType.replaceAnnotation(atypeFactory.getMaybeConstant());
      }
      return super.checkOverride();
    }
  }
}
