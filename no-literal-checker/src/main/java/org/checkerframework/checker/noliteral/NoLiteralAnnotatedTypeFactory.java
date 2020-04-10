package org.checkerframework.checker.noliteral;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;
import org.checkerframework.checker.noliteral.qual.NonConstant;
import org.checkerframework.checker.noliteral.qual.PolyConstant;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.type.visitor.AnnotatedTypeReplacer;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

/** The type factory for the No Literal Checker. */
public class NoLiteralAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The canonical {@code @}{@link @MaybeDerivedFromConstant} annotation. */
  private final AnnotationMirror MAYBE_CONSTANT =
      AnnotationBuilder.fromClass(elements, MaybeDerivedFromConstant.class);

  /** The canonical {@code @}{@link NonConstant} annotation. */
  private final AnnotationMirror NON_CONSTANT =
      AnnotationBuilder.fromClass(elements, NonConstant.class);

  /** The canonical {@code @}{@link PolyConstant} annotation. */
  private final AnnotationMirror POLY = AnnotationBuilder.fromClass(elements, PolyConstant.class);

  /**
   * Map from expression trees representing arrays to their types
   * after local inference from assignments. Types are updated by
   * {@link #modifyTypeAtArrayAccess(Tree)}, and then applied by
   * {@link #addComputedTypeAnnotations(Tree, AnnotatedTypeMirror, boolean)}.
   *
   * Communicating through the expressionTree cache is insufficient, because the
   * updated types might be evicted.
   */
  private final Map<Tree, AnnotatedArrayType> localArrayUpdatedTypes = new HashMap<>();

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
    return new ListTreeAnnotator(super.createTreeAnnotator(), new NoLiteralTreeAnnotator(this));
  }

  @Override
  protected final QualifierDefaults createQualifierDefaults() {
    QualifierDefaults defaults = new NoLiteralQualifierDefaults(elements, this);

    // Ensure that unchecked code is treated optimistically by switching the
    // standard defaults for unchecked code.

    defaults.addUncheckedCodeDefault(POLY, TypeUseLocation.RETURN);

    defaults.addUncheckedCodeDefault(NON_CONSTANT, TypeUseLocation.UPPER_BOUND);
    defaults.addUncheckedCodeDefault(NON_CONSTANT, TypeUseLocation.FIELD);

    // Instead of iterating through QualifierDefaults.STANDARD_UNCHECKED_DEFAULTS_BOTTOM,
    // only switch the default for PARAMETER, because changing LOWER_BOUND is incompatible
    // with the other changes to type variables. See NoLiteralTypeAnnotator's documentation.
    defaults.addUncheckedCodeDefault(POLY, TypeUseLocation.PARAMETER);

    return defaults;
  }

  void modifyTypeAtArrayAccess(Tree tree) {
    ExpressionTree lhs;
    boolean rhsIsMaybeConstant;
    switch (tree.getKind()) {
      case ASSIGNMENT:
        AssignmentTree assign = (AssignmentTree) tree;
        lhs = assign.getVariable();
        rhsIsMaybeConstant = getAnnotatedType(assign.getExpression())
                .hasAnnotation(MAYBE_CONSTANT);
        break;
      case PLUS_ASSIGNMENT:
      case MINUS_ASSIGNMENT:
      case MULTIPLY_ASSIGNMENT:
      case DIVIDE_ASSIGNMENT:
      case REMAINDER_ASSIGNMENT:
      case AND_ASSIGNMENT:
      case OR_ASSIGNMENT:
      case XOR_ASSIGNMENT:
      case RIGHT_SHIFT_ASSIGNMENT:
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
      case LEFT_SHIFT_ASSIGNMENT:
        CompoundAssignmentTree compound = (CompoundAssignmentTree) tree;
        lhs = compound.getVariable();
        rhsIsMaybeConstant = getAnnotatedType(compound.getExpression())
                .hasAnnotation(MAYBE_CONSTANT);
        break;
      case POSTFIX_DECREMENT:
      case POSTFIX_INCREMENT:
      case PREFIX_DECREMENT:
      case PREFIX_INCREMENT:
        UnaryTree unary = (UnaryTree) tree;
        lhs = unary.getExpression();
        rhsIsMaybeConstant = true; // 1 is a constant
        break;
      case VARIABLE:
        return; // handled elsewhere
      default:
        throw new BugInCF("unexpected kind of assignment tree: " + tree.getKind());
    }

    if (lhs.getKind() == Kind.ARRAY_ACCESS) {
      if (rhsIsMaybeConstant) {
        ArrayAccessTree lhsArrayAccess = (ArrayAccessTree) lhs;
        ExpressionTree array = lhsArrayAccess.getExpression();
        // get the actual array, if there is more than one array level
        while (array.getKind() == Kind.ARRAY_ACCESS) {
          array = ((ArrayAccessTree) array).getExpression();
        }
        AnnotatedArrayType arrayType = (AnnotatedArrayType) getAnnotatedType(array);
        AnnotatedTypeMirror componentType = arrayType.getComponentType();
        while (componentType.getKind() == TypeKind.ARRAY) {
          componentType = ((AnnotatedArrayType) componentType).getComponentType();
        }
        Tree decl = declarationFromElement(TreeUtils.elementFromUse(array));
        if (TreeUtils.isLocalVariable(decl)) {
          componentType.replaceAnnotation(MAYBE_CONSTANT);

          // Keep track of the type of both the declaration and the expression
          // representing the array.
          localArrayUpdatedTypes.put(decl, arrayType);
          localArrayUpdatedTypes.put(array, arrayType);

          // also update caches, in case they are hit
          fromMemberTreeCache.put(decl, arrayType);
          fromExpressionTreeCache.put(array, arrayType);
        }
      }
    }
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
     * non-constant strings" -- the latter doesn't even make sense. So to do optimistic defaulting
     * in bytecode correctly, the defaults need to be applied all the way down through an array.
     */
    @Override
    protected DefaultApplierElement createDefaultApplierElement(
        AnnotatedTypeFactory atypeFactory,
        Element annotationScope,
        AnnotatedTypeMirror type,
        boolean applyToTypeVar) {
      // Must check because the defaults for source code must not change to avoid interfering with
      // CLIMB-to-top local type inference.
      boolean fromSource = ElementUtils.isElementFromSourceCode(annotationScope);
      return new NoLiteralDefaultApplierElement(
          atypeFactory, annotationScope, type, applyToTypeVar, fromSource);
    }

    /**
     * Modification of the DefaultApplierElement to also apply defaults to array component types.
     *
     * <p>See summary comment above for reasoning.
     */
    private class NoLiteralDefaultApplierElement extends DefaultApplierElement {

      /** Whether the target type is from bytecode. */
      private final boolean fromSource;

      public NoLiteralDefaultApplierElement(
          AnnotatedTypeFactory atypeFactory,
          Element annotationScope,
          AnnotatedTypeMirror type,
          boolean applyToTypeVar,
          boolean fromSource) {
        super(atypeFactory, annotationScope, type, applyToTypeVar);
        this.fromSource = fromSource;
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
        if (!fromSource && type.getKind() == TypeKind.ARRAY) {
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
    // defaulting rules. This method is therefore overridden to do nothing.
  }

  /**
   * Default unannotated relevant type variables to @MaybeConstant, because it is desirable to
   * assume the worst about e.g. Lists of Strings. This will cause false positives in some cases,
   * but is a work-around until WPI fully supports inferring annotations on type variables from
   * uses.
   */
  private class NoLiteralTypeAnnotator extends TypeAnnotator {
    public NoLiteralTypeAnnotator(NoLiteralAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void aVoid) {
      List<? extends AnnotatedTypeMirror> typeArgs = type.getTypeArguments();
      if (typeArgs.size() > 0) {
        for (AnnotatedTypeMirror typeArg : typeArgs) {
          if (!typeArg.hasExplicitAnnotation(NON_CONSTANT)
              && isRelevantClass(typeArg.getUnderlyingType())) {
            typeArg.replaceAnnotation(MAYBE_CONSTANT);
          }
        }
      }
      return super.visitDeclared(type, aVoid);
    }
  }

  /**
   * Returns the least upper bound of the leaf component types of the array initializer in the no
   * literal hierarchy.
   *
   * @param node a new array expression, such as {@code new int[][] {{5}}}
   * @return the least upper bound of the leaf component types, or null if {@code node} does not
   *     have an initializer (such as {@code new int[0]})
   */
  private @Nullable AnnotationMirror lubOfArrayComponents(NewArrayTree node) {
    AnnotationMirror result = null;
    if (node.getInitializers() == null) {
      return null;
    }
    for (ExpressionTree component : node.getInitializers()) {
      AnnotationMirror componentAnno;
      if (component.getKind() == Kind.NEW_ARRAY) {
        componentAnno = lubOfArrayComponents((NewArrayTree) component);
      } else {
        componentAnno = getAnnotatedType(component).getAnnotationInHierarchy(MAYBE_CONSTANT);
      }
      result =
          result == null ? componentAnno : qualHierarchy.leastUpperBound(result, componentAnno);
    }
    return result;
  }

  /**
   * Returns true if the given type mirror represents a declared type that could have been derived
   * from a literal: a boxed primitive or a string. Returns false for java.lang.Boolean, because
   * this checker does not consider booleans literals.
   *
   * @param type a non-primitive type
   */
  private boolean isRelevantClass(TypeMirror type) {
    return (TypesUtils.isBoxedPrimitive(type) && !TypesUtils.isBooleanType(type))
        || TypesUtils.isString(type);
  }

  private class NoLiteralTreeAnnotator extends TreeAnnotator {
    public NoLiteralTreeAnnotator(NoLiteralAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitNewArray(NewArrayTree node, AnnotatedTypeMirror type) {
      // Even though it is more expensive, this implementation takes the LUB of the
      // leaf component types, regardless of array depth.
      AnnotationMirror lubOfLeafComponents = lubOfArrayComponents(node);

      // Happens when the new array doesn't have an initializer.
      if (lubOfLeafComponents == null) {
        return super.visitNewArray(node, type);
      }

      // Find the innermost component type.
      AnnotatedTypeMirror componentType = ((AnnotatedArrayType) type).getComponentType();
      while (componentType.getKind() == TypeKind.ARRAY) {
        componentType = ((AnnotatedArrayType) componentType).getComponentType();
      }
      componentType.replaceAnnotation(lubOfLeafComponents);

      return super.visitNewArray(node, type);
    }

    @Override
    public Void visitVariable(VariableTree node, AnnotatedTypeMirror type) {
      ExpressionTree initializer = node.getInitializer();
      if (initializer != null && type.getKind() == TypeKind.ARRAY) {
        AnnotatedTypeMirror initializerType = getAnnotatedType(initializer);
        NoLiteralPropagationTypeReplacer replacer = new NoLiteralPropagationTypeReplacer();
        replacer.visit(initializerType, type);
        // Without this, the annotated type factory may continue to use
        // the unannotated version of lhsType for references to the variable
        // later in the method.
        fromMemberTreeCache.put(node, type);
      }
      return super.visitVariable(node, type);
    }
  }

  @Override
  public void addComputedTypeAnnotations(Element elt, AnnotatedTypeMirror type) {
    super.addComputedTypeAnnotations(elt, type);
    if (type.getKind() == TypeKind.ARRAY) {
      Tree decl = declarationFromElement(elt);
      if (fromMemberTreeCache.containsKey(decl)) {
        AnnotatedTypeReplacer.replace(fromMemberTreeCache.get(decl), type);
      }
    }
  }

  @Override
  protected void addComputedTypeAnnotations(Tree tree, AnnotatedTypeMirror type, boolean iUseFlow) {
    super.addComputedTypeAnnotations(tree, type, iUseFlow);
    if (type.getKind() == TypeKind.ARRAY) {
      AnnotatedArrayType updatedType = localArrayUpdatedTypes.get(tree);
      if (updatedType != null) {
        AnnotatedTypeReplacer.replace(updatedType, type);
      }
    }
  }

  /**
   * An annotated type merger that merges no literal annotations and only if the type that is
   * receiving an annotation has an @MaybeDerivedFromConstant annotation or NO annotations.
   */
  private class NoLiteralPropagationTypeReplacer extends AnnotatedTypeReplacer {
    @Override
    protected void replaceAnnotations(AnnotatedTypeMirror from, AnnotatedTypeMirror to) {
      final AnnotationMirror fromNoLiteralAnno = from.getAnnotationInHierarchy(MAYBE_CONSTANT);
      final AnnotationMirror toNoLiteralAnno = to.getAnnotationInHierarchy(MAYBE_CONSTANT);

      boolean toNeedsAnnotation =
          toNoLiteralAnno == null || AnnotationUtils.areSame(toNoLiteralAnno, MAYBE_CONSTANT);
      if (fromNoLiteralAnno != null && toNeedsAnnotation) {
        to.replaceAnnotation(fromNoLiteralAnno);
      }
    }
  }
}
