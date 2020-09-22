// This test makes sure that overriding a JDK method (or any other
// method defined in bytecode) does not cause a spurious
// override.param.invalid error.

class OverrideJDKMethod extends Thread {
  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public ClassLoader getContextClassLoader() {
    return super.getContextClassLoader();
  }

  @Override
  public void setContextClassLoader(ClassLoader cl) {
    super.setContextClassLoader(cl);
  }
}
