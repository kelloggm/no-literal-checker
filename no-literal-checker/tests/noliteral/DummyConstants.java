import java.util.Arrays;

import org.checkerframework.checker.noliteral.qual.*;

// All @MaybeDerivedFromConstant annotations come from running WPI on this code, none were
// written by hand. @NonConstant annotations were written by me for testing.

/**
 * A class storing a hard-coded string and a hard-coded byte[]
 */
public final class DummyConstants {

  /**
   * A hard-coded string for testing
   */
  public static final @MaybeDerivedFromConstant String SECRET = "sfsdfs";
  // :: error: assignment.type.incompatible
  public static final @NonConstant String SECRET2 = "sfsdfs";

  /**
   * A hard-coded byte[] for testing
   */
  static final @MaybeDerivedFromConstant byte[] KEY = {11, -19, -79, 58, -21, 90, 20, 85, 59, -15, -11, 89, 112, -65, 105};
  private final @MaybeDerivedFromConstant byte[] key;

  // :: error: assignment.type.incompatible :: error: array.initializer.type.incompatible
  static final @NonConstant byte[] KEY2 = {11, -19, -79, 58, -21, 90, 20, 85, 59, -15, -11, 89, 112, -65, 105};
  private final @NonConstant byte[] key2;

  public DummyConstants() {
    key = new byte[]{11, -19, -79, 58, -21, 90, 20, 85, 59, -15, -11, 89, 112, -65, 105};

    // :: error: assignment.type.incompatible
    key2 = new byte[]{11, -19, -79, 58, -21, 90, 20, 85, 59, -15, -11, 89, 112, -65, 105};
  }

  /**
   * Get the hard-coded string
   *
   * @return the hard-coded string
   */
  public static @MaybeDerivedFromConstant String getConstant() {
    return SECRET;
  }

  public static @NonConstant String getConstant2() {
    // :: error: return.type.incompatible
    return SECRET;
  }

  /**
   * Get a copy of the hard-coded byte[] which is a static field (Arrays.copyOf is specified as flow-through method)
   *
   * @return copy of the hard-coded byte[] which is a static field
   */
  public static @MaybeDerivedFromConstant byte @MaybeDerivedFromConstant [] getBytes() {
    return Arrays.copyOf(KEY, KEY.length + 1);
  }

  public static @NonConstant byte @NonConstant [] getBytes2() {
    // :: error: return.type.incompatible
    return Arrays.copyOf(KEY, KEY.length + 1);
  }

  /**
   * Get a copy of the hard-coded byte[] which is an instance field
   *
   * @return copy of the hard-coded byte[] which is an instance field
   */
  public @MaybeDerivedFromConstant byte @MaybeDerivedFromConstant [] getKey() {
    return Arrays.copyOf(key, key.length + 1);
  }

  public @NonConstant byte @NonConstant [] getKey2() {
    // :: error: return.type.incompatible
    return Arrays.copyOf(key, key.length + 1);
  }

}
