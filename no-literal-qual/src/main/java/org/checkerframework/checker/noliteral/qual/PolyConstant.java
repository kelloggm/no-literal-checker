package org.checkerframework.checker.noliteral.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.PolymorphicQualifier;

/**
 * A polymorphic qualifier for the no literal type system.
 *
 * <p>See <a href="https://checkerframework.org/manual/#qualifier-polymorphism">the Checker
 * Framework manual</a> section on polymorphic types.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(MaybeDerivedFromConstant.class)
public @interface PolyConstant {}
