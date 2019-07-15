package org.checkerframework.checker.noliteral.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * The top type for the NoLiteral Checker's type system.
 *
 * <p>This type represents an expression that might contain a literal, like 5 or {0xa, 0xb}. It is
 * inferred for all manifest literals in the program.
 *
 * <p>This is also the type of any expression that contains a subexpression that may be a constant.
 * For example, the type of <code>"hello" + x</code>, where <code>x</code> is a non-constant String,
 * is <code>@MaybeConstant String</code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@QualifierForLiterals(LiteralKind.ALL)
@DefaultInUncheckedCodeFor({TypeUseLocation.PARAMETER, TypeUseLocation.LOWER_BOUND})
@SubtypeOf({})
public @interface MaybeConstant {}
