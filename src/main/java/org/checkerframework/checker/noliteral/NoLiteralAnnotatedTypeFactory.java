package org.checkerframework.checker.noliteral;

import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;
import org.checkerframework.checker.noliteral.qual.NonConstant;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.javacutil.AnnotationBuilder;

/**
 * The type factory for the no literal checker. It doesn't do much: all it provides are some
 * canonical annotations. If we want to change this checker so that it uses the Value Checker for
 * defaulting, we would do it here.
 */
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

  public AnnotationMirror getBottom() {
    return NON_CONSTANT;
  }

  public AnnotationMirror getTop() {
    return MAYBE_CONSTANT;
  }

  @Override
  protected void checkInvalidOptionsInferSignatures() {
    // This checker is specifically designed to work with whole-program inference,
    // so it can turn off the defensive check in WPI that requires certain bytecode
    // defaulting rules. This method is therefore overwritten to do nothing.
  }
}
