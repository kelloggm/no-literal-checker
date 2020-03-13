package org.checkerframework.checker.noliteral;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;
import org.checkerframework.checker.noliteral.qual.NonConstant;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.ElementUtils;

/** The type factory for the no literal checker. */
public class NoLiteralAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The canonical maybe-constant annotation */
  private final AnnotationMirror MAYBE_CONSTANT =
      AnnotationBuilder.fromClass(elements, MaybeDerivedFromConstant.class);

  /** The canonical non-constant annotation */
  private final AnnotationMirror NON_CONSTANT =
      AnnotationBuilder.fromClass(elements, NonConstant.class);

  public NoLiteralAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    this.postInit();
  }

  /** @return a canonical non-constant AnnotationMirror */
  public AnnotationMirror getNonConstant() {
    return NON_CONSTANT;
  }

  /** @return a canonical maybe-constant AnnotationMirror */
  public AnnotationMirror getMaybeConstant() {
    return MAYBE_CONSTANT;
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new NoLiteralTypeAnnotator(this), super.createTypeAnnotator());
  }

  @Override
  protected TreeAnnotator createTreeAnnotator() {
    return new ListTreeAnnotator(new NoLiteralTreeAnnotator(this), super.createTreeAnnotator());
  }

  @Override
  protected final QualifierDefaults createQualifierDefaults() {
    QualifierDefaults defaults = new NoLiteralQualifierDefaults(elements, this);
    // Ensure that unchecked code is treated optimistically by switching the
    // standard defaults for unchecked code.
    defaults.addUncheckedCodeDefault(MAYBE_CONSTANT, TypeUseLocation.PARAMETER);
    // defaults.addUncheckedCodeDefault(MAYBE_CONSTANT, TypeUseLocation.LOWER_BOUND);
    defaults.addUncheckedCodeDefault(NON_CONSTANT, TypeUseLocation.RETURN);
    defaults.addUncheckedCodeDefault(NON_CONSTANT, TypeUseLocation.UPPER_BOUND);
    defaults.addUncheckedCodeDefault(NON_CONSTANT, TypeUseLocation.FIELD);
    return defaults;
  }

  /**
   * An extension to QualifierDefaults that does nothing instead of adding the standard unchecked
   * qualifier defaults, since they override the defaults added above for PARAMETER.
   *
   * <p>TODO: is that a bug in QualifierDefaults? It looks like it's not supposed to override
   * anything...
   */
  private class NoLiteralQualifierDefaults extends QualifierDefaults {
    public NoLiteralQualifierDefaults(
        Elements elements, NoLiteralAnnotatedTypeFactory noLiteralAnnotatedTypeFactory) {
      super(elements, noLiteralAnnotatedTypeFactory);
    }

    /** Add standard unchecked defaults that do not conflict with previously added defaults. */
    @Override
    public void addUncheckedStandardDefaults() {
      // do nothing
    }

    /**
     * Replace the normal default applier element with one that also applies the defaults to the
     * component types of arrays. For the no-literal checker, it makes a lot more sense to talk
     * about "a non-constant array of string constants" than it does to say "a constant array of
     * non-constant strings" - the latter doesn't even make sense. So to do optimistic defaulting in
     * bytecode correctly, the defaults need to be applied all the way down through an array.
     */
    @Override
    protected DefaultApplierElement createDefaultApplierElement(
        AnnotatedTypeFactory atypeFactory,
        Element annotationScope,
        AnnotatedTypeMirror type,
        boolean applyToTypeVar) {
      // Must check because the defaults for source code must not change to avoid interfering with
      // CLIMB-to-top
      // local type inference.
      if (ElementUtils.isElementFromByteCode(annotationScope)) {
        return new NoLiteralDefaultApplierElement(
            atypeFactory, annotationScope, type, applyToTypeVar);
      } else {
        return super.createDefaultApplierElement(
            atypeFactory, annotationScope, type, applyToTypeVar);
      }
    }

    /**
     * Modification of the DefaultApplierElement to also apply defaults to array component types.
     *
     * <p>See summary comment above for reasoning.
     */
    private class NoLiteralDefaultApplierElement extends DefaultApplierElement {

      public NoLiteralDefaultApplierElement(
          AnnotatedTypeFactory atypeFactory,
          Element annotationScope,
          AnnotatedTypeMirror type,
          boolean applyToTypeVar) {
        super(atypeFactory, annotationScope, type, applyToTypeVar);
      }

      /**
       * Add the qualifier to the type if it does not already have an annotation in the same
       * hierarchy as qual.
       *
       * @param type type to add qual
       * @param qual annotation to add
       */
      @Override
      protected void addAnnotation(AnnotatedTypeMirror type, AnnotationMirror qual) {
        super.addAnnotation(type, qual);
        if (type.getKind() == TypeKind.ARRAY) {
          AnnotatedArrayType asArrayType = (AnnotatedArrayType) type;
          addAnnotation(asArrayType.getComponentType(), qual);
        }
      }
    }
  }

  @Override
  protected void checkInvalidOptionsInferSignatures() {
    // This checker is specifically designed to work with whole-program inference,
    // so it can turn off the defensive check in WPI that requires certain bytecode
    // defaulting rules. This method is therefore overwritten to do nothing.
  }

  /**
   * Default unannotated type variables to @MaybeConstant, because it is desirable to assume the
   * worst about e.g. Lists of Strings.
   */
  private class NoLiteralTypeAnnotator extends TypeAnnotator {
    public NoLiteralTypeAnnotator(NoLiteralAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitTypeVariable(AnnotatedTypeVariable type, Void aVoid) {

      if (!type.hasExplicitAnnotation(NON_CONSTANT)) {
        type.replaceAnnotation(MAYBE_CONSTANT);
      }

      return super.visitTypeVariable(type, aVoid);
    }

    /**
     * Default type variables in return types of user-written methods to MaybeConstant, for
     * consistency with other defaulting of type variables.
     */
    @Override
    public Void visitExecutable(AnnotatedExecutableType method, Void aVoid) {
      AnnotatedTypeMirror returnType = method.getReturnType();
      if (returnType != null && returnType.getKind() == TypeKind.DECLARED) {
        AnnotatedDeclaredType asDeclared = (AnnotatedDeclaredType) returnType;
        List<? extends AnnotatedTypeMirror> typeArgs = asDeclared.getTypeArguments();
        if (typeArgs.size() > 0) {
          for (AnnotatedTypeMirror typeArg : typeArgs) {
            if (!typeArg.hasExplicitAnnotation(NON_CONSTANT)) {
              typeArg.replaceAnnotation(MAYBE_CONSTANT);
            }
          }
        }
      }
      return super.visitExecutable(method, aVoid);
    }
  }

  /**
   * Default unannotated type variables in local variable declarations to @MaybeConstant, because it
   * is desirable to assume the worst about e.g. Lists of Strings.
   */
  private class NoLiteralTreeAnnotator extends TreeAnnotator {
    public NoLiteralTreeAnnotator(NoLiteralAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitVariable(VariableTree node, AnnotatedTypeMirror annotatedTypeMirror) {
      if (node.getType().getKind() == Kind.PARAMETERIZED_TYPE) {
        AnnotatedDeclaredType asDeclared = (AnnotatedDeclaredType) annotatedTypeMirror;
        for (AnnotatedTypeMirror atm : asDeclared.getTypeArguments()) {
          if (!atm.hasExplicitAnnotation(NON_CONSTANT)) {
            atm.replaceAnnotation(MAYBE_CONSTANT);
          }
        }
      }
      return super.visitVariable(node, annotatedTypeMirror);
    }
  }
}