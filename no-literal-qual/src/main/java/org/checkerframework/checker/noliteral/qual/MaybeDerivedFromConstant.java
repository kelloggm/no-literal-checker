package org.checkerframework.checker.noliteral.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * This type represents an expression that may have been derived (in part) from a literal, like {@code 5}, {@code "hello"},  or {@code {0xa, 0xb}}.

 * This is also the type of any expression that contains a subexpression that may be a constant. For
 * example, the type of <code>"hello" + x</code> is always
 * <code>@MaybeDerivedFromConstant String</code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
// This list intentionally excludes null and boolean, because they aren't very interesting
@QualifierForLiterals({
  LiteralKind.CHAR,
  LiteralKind.DOUBLE,
  LiteralKind.FLOAT,
  LiteralKind.INT,
  LiteralKind.LONG,
  LiteralKind.STRING
})
@DefaultFor({TypeUseLocation.UPPER_BOUND})
@SubtypeOf({})
public @interface MaybeDerivedFromConstant {}
