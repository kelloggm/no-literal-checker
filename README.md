### No Literal Checker

This checker proves that no literal or literal-derived values can flow to
specified APIs in a Java codebase. It can enforce security and compliance
rules like "do not hard-code credentials".

### What problem does this solve?

For security, the value
passed to some APIs must never be a constant. For example, cryptographic keys
and passwords need to come from user configuration or computation, not from a
hard-coded constant.

### What is considered a hard-coded constant?

Any manifest literal of types `short`, `int`, `long`, `byte`, `char`, `float`,
`double`, or `String`, or any array of these, is considered a hard-coded constant.
Examples include `1`, `"hello"`, and `{0xa, 0xb}`. Values derived from other hard-coded
constants by arithmetic-like operations (including e.g. string concatenation) are also
considered hard-coded. For example `1 + x` is a hard-coded constant, even if `x` is not.
This rule is conservative: it might overestimate which values in the program are hard-coded.
It is necessary, though, because an expression like `x * 0` really is hard-coded,
even if `x` is not.

Note that `boolean`s are *not* considered constants, because they only have two values
and are not interesting for the security properties this checker is meant to track.
`null` is also *not* considered a constant; for null-tracking, see the [Nullness
Checker](https://checkerframework.org/manual/#nullness-checker) of the Checker Framework.

### Annotating your program

The checker assigns each expression in the program one of
these two types:

```java
@MaybeDerivedFromConstant
            |
       @NonConstant
```

The default is `@NonConstant` for all expressions except manifest literals.
Because method parameters default to `@NonConstant`, the user must write a `@MaybeDerivedFromConstant` annotation on the definition
of any method that may be called with a constant argument. For example, if your code includes
the correct call:
```java
foo(5);
```
then the defintion of `foo` will need to be annotated like so:
```java
void foo(@MaybeDerivedFromConstant int x) { }
```

### You must annotate libraries

For unchecked code (that is, code for which
only the bytecode is available, such as code from a `.jar` file), the following
optimistic defaulting rules are applied:
* the return type of any function defined in a library is `@NonConstant` if the function takes no parameters, or all of its parameters are non-constant
* the types of a library function's formal parameters are always `@MaybeDerivedFromConstant`

unless a stub file is supplied that overwrites this default

As a consequence of these rules, users MUST always write stub files for libraries
they intend to protect -- this checker is **useless** without such a stub.

For example, [one of the No Literal Checker's built-in stub
files](https://github.com/kelloggm/no-literal-checker/blob/prototype/no-literal-checker/stubs/crypto.astub)
enforces that calls to the `javax.crypto.spec.SecretKeySpec` constructor only
ever provide non-constant keys:

```java
package javax.crypto.spec;

import org.checkerframework.checker.noliteral.qual.NonConstant;

class SecretKeySpec {
    SecretKeySpec(@NonConstant byte[] key, String algorithm);
    SecretKeySpec(@NonConstant byte[] key, int offset, int len, String algorithm);
}
```

For more about stub files, see the
[Checker Framework manual](https://checkerframework.org/manual/#stub).

### Local type inference for array component types

Unlike most typecheckers built on the Checker Framework, this checker includes custom rules
for inferring the innermost component types of local arrays. That is, the checker can infer that
the type of `arr1` in the following code should be `@MaybeDerivedFromConstant int []`, but 
that the type of `arr2` should be `@NonConstant int[]`:

```java
void test(@NonConstant int x) {
  int[] arr1 = new int[] {1, 2, 3};
  int[] arr2 = new int[] {x, x, x};
}
```

### Using the checker

The Checker Framework manual explains how to [integrate with external tools](https://checkerframework.org/manual/#external-tools).

Due to the high annotation burden imposed by this checker, it is recommended that you
run the checker using a type inference tool, such as
[whole-program inference](https://checkerframework.org/manual/#type-inference).
