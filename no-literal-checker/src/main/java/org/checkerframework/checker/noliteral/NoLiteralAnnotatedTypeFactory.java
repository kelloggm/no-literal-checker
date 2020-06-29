package org.checkerframework.checker.noliteral;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
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

  /** The canonical {@code @}{@link MaybeDerivedFromConstant} annotation. */
  private final AnnotationMirror MAYBE_CONSTANT =
      AnnotationBuilder.fromClass(elements, MaybeDerivedFromConstant.class);

  /** The canonical {@code @}{@link NonConstant} annotation. */
  private final AnnotationMirror NON_CONSTANT =
      AnnotationBuilder.fromClass(elements, NonConstant.class);

  /** The canonical {@code @}{@link PolyConstant} annotation. */
  private final AnnotationMirror POLY = AnnotationBuilder.fromClass(elements, PolyConstant.class);

  /**
   * Map from expression trees representing arrays to their types after local inference from
   * assignments. Types are updated by {@link #modifyTypeAtArrayAccess(Tree)}, and then applied by
   * {@link #addComputedTypeAnnotations(Tree, AnnotatedTypeMirror, boolean)}.
   *
   * <p>Communicating through the expressionTree cache is insufficient, because the updated types
   * might be evicted.
   */
  private final Map<Tree, AnnotatedArrayType> localArrayUpdatedTypes = new HashMap<>();

  public NoLiteralAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    this.postInit();
  }

  /**
   * Returns a canonical {@code @}{@link NonConstant} annotation.
   *
   * @return a canonical {@code @}{@link NonConstant} annotation
   */
  public AnnotationMirror getNonConstant() {
    return NON_CONSTANT;
  }

  /**
   * Returns a canonical {@code @}{@link MaybeDerivedFromConstant} annotation.
   *
   * @return a canonical {@code @}{@link MaybeDerivedFromConstant} annotation
   */
  public AnnotationMirror getMaybeConstant() {
    return MAYBE_CONSTANT;
  }

  /**
   * Returns a canonical {@code @}{@link PolyConstant} annotation.
   *
   * @return a canonical {@code @}{@link PolyConstant} annotation
   */
  public AnnotationMirror getPolyConstant() {
    return POLY;
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

    // The defaulting scheme for methods is polymorphic so long as they take/give "relevant"
    // types - those that might be manifest literals. It is optimistic otherwise; see the
    // NoLiteralDefaultElementApplier.
    defaults.addUncheckedCodeDefault(POLY, TypeUseLocation.RETURN);
    defaults.addUncheckedCodeDefault(POLY, TypeUseLocation.PARAMETER);

    // Fields are assumed to be non-constant. This default is unsound, but dataflow
    // through fields is rare. TODO: make this sound
    defaults.addUncheckedCodeDefault(NON_CONSTANT, TypeUseLocation.FIELD);

    return defaults;
  }

  /**
   * Implementation of local type inference for array component types, given a tree which might
   * contain an assignment to an array element.
   *
   * @param tree a tree that represents some kind of assignment
   */
  /* package-private */ void modifyTypeAtArrayAccess(Tree tree) {
    ExpressionTree lhs;
    boolean rhsIsMaybeConstant;
    switch (tree.getKind()) {
      case ASSIGNMENT:
        AssignmentTree assign = (AssignmentTree) tree;
        lhs = assign.getVariable();
        rhsIsMaybeConstant = getAnnotatedType(assign.getExpression()).hasAnnotation(MAYBE_CONSTANT);
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
        rhsIsMaybeConstant =
            getAnnotatedType(compound.getExpression()).hasAnnotation(MAYBE_CONSTANT);
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

  /** An extension to QualifierDefaults that also defaults the component types of arrays. */
  private class NoLiteralQualifierDefaults extends QualifierDefaults {
    public NoLiteralQualifierDefaults(
        Elements elements, NoLiteralAnnotatedTypeFactory noLiteralAnnotatedTypeFactory) {
      super(elements, noLiteralAnnotatedTypeFactory);
    }

    @Override
    public boolean applyConservativeDefaults(Element annotationScope) {
      if (annotationScope == null) {
        return false;
      }

      boolean isFromStubFile = isFromStubFile(annotationScope);
      boolean isBytecode =
          ElementUtils.isElementFromByteCode(annotationScope)
              && declarationFromElement(annotationScope) == null
              && !isFromStubFile;

      // Correct mislabeling of JDK code as source code when using --release 8.
      if (!isBytecode && !ElementUtils.isElementFromSourceCode(annotationScope)) {
        return true;
      }

      return super.applyConservativeDefaults(annotationScope);
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
        if (fromSource) {
          super.addAnnotation(type, qual);
        } else {
          // Use polymorphic defaulting for possibly-literal types.
          // For other types, use optimistic defaulting; this code implements that.
          if (!isPossiblyLiteralType(type.getUnderlyingType())) {
            switch (location) {
              case RETURN:
                qual = NON_CONSTANT;
                break;
              case PARAMETER:
                qual = MAYBE_CONSTANT;
                break;
              default:
                // don't change qual
                break;
            }
          }
          super.addAnnotation(type, qual);
          if (type.getKind() == TypeKind.ARRAY) {
            AnnotatedArrayType asArrayType = (AnnotatedArrayType) type;
            addAnnotation(asArrayType.getComponentType(), qual);
          }
        }
      }
    }
  }

  @Override
  protected void checkInvalidOptionsInferSignatures() {
    // This checker is specifically designed to work with whole-program inference,
    // so it can turn off the defensive check in -AinferI that requires certain bytecode
    // defaulting rules. This method is therefore overridden to do nothing.
  }

  /**
   * Default unannotated relevant type variables to @MaybeConstant, because it is desirable to
   * assume the worst about e.g. Lists of Strings. This will cause false positives in some cases,
   * but is a work-around until -Ainfer fully supports inferring annotations on type variables from
   * uses.
   */
  private class NoLiteralTypeAnnotator extends TypeAnnotator {
    public NoLiteralTypeAnnotator(NoLiteralAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void aVoid) {
      List<? extends AnnotatedTypeMirror> typeArgs = type.getTypeArguments();
      if (!typeArgs.isEmpty()) {
        for (AnnotatedTypeMirror typeArg : typeArgs) {
          if (!typeArg.hasExplicitAnnotation(NON_CONSTANT)
              && isPossiblyLiteralType(typeArg.getUnderlyingType())) {
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
    if (node.getInitializers() == null) {
      return null;
    }
    AnnotationMirror result = null;
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
   * Returns true if the given type mirror represents a type that includes values that could be
   * derived from a literal: a boxed primitive, string, or java.lang.Object. Returns false for
   * java.lang.Boolean, because this checker does not consider booleans literals.
   *
   * @param type a non-primitive type
   */
  private boolean isPossiblyLiteralType(TypeMirror type) {
    return (TypesUtils.isBoxedPrimitive(type) && !TypesUtils.isBooleanType(type))
        || TypesUtils.isString(type)
        || TypesUtils.isObject(type);
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
   * An annotated type replacer that replaces no literal annotations only if the type that is
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
