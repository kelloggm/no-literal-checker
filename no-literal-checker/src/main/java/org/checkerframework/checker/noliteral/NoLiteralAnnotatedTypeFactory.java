package org.checkerframework.checker.noliteral;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;
import org.checkerframework.checker.noliteral.qual.NonConstant;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
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
   * qualifier defaults, since they override the defaults added above for PARAMETER and LOWER_BOUND.
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
  }

  @Override
  protected void checkInvalidOptionsInferSignatures() {
    // This checker is specifically designed to work with whole-program inference,
    // so it can turn off the defensive check in WPI that requires certain bytecode
    // defaulting rules. This method is therefore overwritten to do nothing.
  }
}
