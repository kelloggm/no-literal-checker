package org.checkerframework.checker.noliteral;

import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;
import org.checkerframework.checker.noliteral.qual.NonConstant;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.javacutil.AnnotationBuilder;

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
  protected void checkInvalidOptionsInferSignatures() {
    // This checker is specifically designed to work with whole-program inference,
    // so it can turn off the defensive check in WPI that requires certain bytecode
    // defaulting rules. This method is therefore overwritten to do nothing.
  }
}
