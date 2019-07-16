// This test makes sure that overriding a JDK method (or any other
// method defined in bytecode) does not cause a spurious
// override.param.invalid error.

class OverrideJDKMethod {
    @Override
    public boolean equals(Object other) {
        return true;
    }
}