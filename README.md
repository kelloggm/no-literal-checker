### No Literal Checker

A typechecker for proving that no literal values can flow to specified APIs in
a Java codebase. Useful for enforcing security and compliance rules like 
"do not hard-code credentials".

### What problem does this solve?

Many interesting security/compliance properties are requirements that the value
passed to a particular API never be a constant. For example, cryptographic keys 
and passwords need to come from user configuration or computation, not from a 
hard-coded constant.

### How does it work?

The checker is built on the [Checker Framework](checkerframework.org).

The type system is:

```java
@MaybeConstant
      |
 @NonConstant
```

The default is `@NonConstant` for all expressions except manifest literals (examples
of manifest literals include `1`, `"hello"`, and `{0xa, 0xb}`). That default includes
method parameters, so a user should expect to need to write an annotation on the defintion
of any method that is called with a constant argument. For example, if your code includes
the call:
```java
foo(5);
```
then the defintion of `foo` will need to be annotated like so:
```java
void foo(@MaybeConstant int x) { }
```

The default is slightly different in unchecked code (that is, code for which
only the bytecode is available, such as code from a `.jar` file). The following
optimistic defaulting rules are applied:
* the return type of any function defined in a library is `@NonConstant`
* the types of a libraries' formal parameters are always `@MaybeConstant` unless
a stub file is supplied that overwrites this default

As a consequence of these rules, users MUST always write stub files for libraries
they intend to protect - this checker is **useless** without such a stub.

For example, to enforce that calls to `javax.crypto.spec.SecretKeySpec`'s constructor
only ever provide non-constant keys, you would use a stub file like this:

```java
package javax.crypto.spec;

import org.checkerframework.checker.noliteral.qual.NonConstant;

class SecretKeySpec {
    SecretKeySpec(@NonConstant byte [] key, String algorithm);
    SecretKeySpec(@NonConstant byte[] key, int offset, int len, String algorithm);
}
```

For more about stub files, see the 
[Checker Framework manual section](https://checkerframework.org/manual/#stub).
