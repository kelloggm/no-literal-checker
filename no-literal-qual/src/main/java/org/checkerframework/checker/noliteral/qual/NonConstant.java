package org.checkerframework.checker.noliteral.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * An expression with this type, and its subexpressions, were definitely not derived from any
 * manifest literals.
 *
 * <p>This is the default type in user-written code.
 *
 * <p>All calls external to the application (i.e. into unchecked class files) are assumed to return
 * values of this type.
 */
@SubtypeOf({MaybeDerivedFromConstant.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@DefaultQualifierInHierarchy
public @interface NonConstant {}
