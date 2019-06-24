package org.checkerframework.checker.noliteral.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

/** The bottom type for the NoLiteral Checker's type system. */
@SubtypeOf({MaybeConstant.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@DefaultFor({TypeUseLocation.EXCEPTION_PARAMETER, TypeUseLocation.IMPLICIT_UPPER_BOUND})
@TargetLocations({TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND})
@DefaultQualifierInHierarchy
@DefaultInUncheckedCodeFor({
  TypeUseLocation.RETURN,
  TypeUseLocation.FIELD,
  TypeUseLocation.UPPER_BOUND
})
public @interface NonConstant {}
