package org.checkerframework.checker.noliteral;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;
import org.checkerframework.checker.noliteral.qual.NonConstant;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.framework.type.visitor.AnnotatedTypeScanner;
import org.checkerframework.javacutil.ElementUtils;

/**
 * This visitor has a lot of code in it that permits this type system to operate as expected with
 * the bottom type as its default. Most of that code is either from or inspired by the AWS Data
 * Classification Checker (https://github.com/awslabs/data-classification-checker).
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
   * Overrides the default lower bound for exception parameters. The usual default for exception
   * parameter lower bounds is top, which prevents the checker from defaulting exception parameters
   * to @NonConstant. Overriding this method changes the expected lower bound to be @NonConstant,
   * which prevents false positives.
   *
   * <p>Stolen shamelessly from https://github.com/awslabs/data-classification-checker
   *
   * @return a singleton set containing the @NonConstant annotation
   */
  @Override
  protected Set<? extends AnnotationMirror> getExceptionParameterLowerBoundAnnotations() {
    return Collections.singleton(atypeFactory.getNonConstant());
  }

  /**
   * Overrides the default upper bound for thrown exceptions. By default, this is the same as the
   * result of getExceptionParameterLowerBoundAnnotations, which defaults to top. This is overridden
   * to keep the default top, since that other method is also overridden.
   *
   * <p>Stolen shamelessly from https://github.com/awslabs/data-classification-checker
   *
   * @return a singleton set containing the @MaybeDerivedFromConstant annotation
   */
  @Override
  protected Set<? extends AnnotationMirror> getThrowUpperBoundAnnotations() {
    return Collections.singleton(atypeFactory.getMaybeConstant());
  }

  /**
   * Searches through a type for user-written (i.e. non-default) annotations. Returns true if any
   * are found.
   *
   * <p>Usage: NonDefaultScanner scanner = new NonDefaultScanner(); scanner.visit(type); boolean
   * result = scanner.getResult();
   */
  private class NonDefaultScanner extends AnnotatedTypeScanner<Void, Void> {

    private boolean result = false;

    /**
     * After calling visit(), this will return whether or not the visited type(s) have any non
     * default annotations.
     */
    public boolean getResult() {
      return result;
    }

    @Override
    protected Void scan(AnnotatedTypeMirror type, Void aVoid) {
      if (!type.hasAnnotation(NonConstant.class)) {
        result = true;
      }
      return super.scan(type, aVoid);
    }
  }

  /**
   * Searches through a type for user-written (i.e. non-default) annotations. Returns true if any
   * are found.
   *
   * @param atm the type to search
   * @return true if the type or any of its components has a non-default annotation
   */
  private boolean hasNonDefault(final AnnotatedTypeMirror atm) {
    NonDefaultScanner scanner = new NonDefaultScanner();
    scanner.visit(atm);
    return scanner.getResult();
  }

  /**
   * This checker defaults implicit upper bounds of type variables to @NonConstant. This is the
   * correct thing to do in unannotated code, but causes false positive errors whenever a user
   * writes a type annotation on the upper bound of a type variable (for instance, by declaring a
   * list of constant strings).
   *
   * <p>For example, without overriding this method, this code would not typecheck:
   *
   * <p>{@code List<@MaybeDerivedFromConstant String> list = new ArrayList<>();}
   *
   * <p>Since this is common, this code disables checking that type arguments supplied to a type or
   * a method invocation are within the bounds of the type variables as declared for user-written
   * annotations. Stolen shamelessly from https://github.com/awslabs/data-classification-checker
   */
  @Override
  protected void checkTypeArguments(
      final Tree toptree,
      final List<? extends AnnotatedTypeParameterBounds> paramBounds,
      final List<? extends AnnotatedTypeMirror> typeargs,
      final List<? extends Tree> typeargTrees) {
    List<AnnotatedTypeParameterBounds> newParamBounds = new ArrayList<>();
    List<AnnotatedTypeMirror> newTypeArgs = new ArrayList<>();
    for (int i = 0; i < typeargs.size(); i++) {
      AnnotatedTypeMirror atm = typeargs.get(i);
      if (!hasNonDefault(atm)) {
        newParamBounds.add(paramBounds.get(i));
        newTypeArgs.add(atm);
      }
    }
    if (!newTypeArgs.isEmpty()) {
      super.checkTypeArguments(toptree, newParamBounds, newTypeArgs, typeargTrees);
    }
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
   * when overriding methods defined in bytecode (which therefore use a different, incompatible
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
      if (ElementUtils.isElementFromByteCode(overridden.getElement())) {
        boolean[] replaced = new boolean[overridden.getParameterTypes().size()];
        List<AnnotatedTypeMirror> paramTypes = overridden.getParameterTypes();
        for (int i = 0; i < paramTypes.size(); i++) {
          AnnotatedTypeMirror paramType = paramTypes.get(i);
          if (paramType.getAnnotation(MaybeDerivedFromConstant.class) != null) {
            paramType.replaceAnnotation(atypeFactory.getNonConstant());
            replaced[i] = true;
          } else {
            replaced[i] = false;
          }
        }
        boolean result = super.checkOverride();
        for (int i = 0; i < paramTypes.size(); i++) {
          if (replaced[i]) {
            AnnotatedTypeMirror paramType = paramTypes.get(i);
            paramType.replaceAnnotation(atypeFactory.getMaybeConstant());
          }
        }
        return result;
      } else {
        return super.checkOverride();
      }
    }
  }
}