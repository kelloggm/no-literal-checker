package org.checkerframework.checker.noliteral;

import com.sun.source.tree.NewClassTree;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.noliteral.qual.MaybeConstant;
import org.checkerframework.checker.noliteral.qual.NonConstant;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;

/**
 * The type factory for the no literal checker. It doesn't do much: all it provides are some
 * canonical annotations. If we want to change this checker so that it uses the Value Checker for
 * defaulting, we would do it here.
 */
public class NoLiteralAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The canonical maybe-constant annotation */
  private final AnnotationMirror MAYBE_CONSTANT =
      AnnotationBuilder.fromClass(elements, MaybeConstant.class);

  /** The canonical non-constant annotation */
  private final AnnotationMirror NON_CONSTANT =
      AnnotationBuilder.fromClass(elements, NonConstant.class);

  public NoLiteralAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    this.postInit();
  }

  @Override
  public TreeAnnotator createTreeAnnotator() {
    return new ListTreeAnnotator(super.createTreeAnnotator(), new NoLiteralTreeAnnotator(this));
  }

  public AnnotationMirror getCanonicalBottomAnnotation() {
    return NON_CONSTANT;
  }

  public AnnotationMirror getCanonicalTopAnnotation() {
    return MAYBE_CONSTANT;
  }

  private class NoLiteralTreeAnnotator extends TreeAnnotator {
    public NoLiteralTreeAnnotator(NoLiteralAnnotatedTypeFactory atypeFactory) {
      super(atypeFactory);
    }

    /**
     * The default for new objects should be @NonConstant, because this checker is only interested
     * in primitive and String literals.
     */
    @Override
    public Void visitNewClass(NewClassTree tree, AnnotatedTypeMirror type) {
      type.replaceAnnotation(NON_CONSTANT);
      return super.visitNewClass(tree, type);
    }
  }
}
