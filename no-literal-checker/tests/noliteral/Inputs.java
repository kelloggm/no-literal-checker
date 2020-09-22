// a test that various ways for user-input to enter the program are treated as @NonConstant.

import org.checkerframework.checker.noliteral.qual.*;

import javax.swing.JOptionPane;

class Inputs {
  void testSystem() {
    @NonConstant String variable = System.getenv("CHECKERFRAMEWORK");
    @NonConstant String property = System.getProperty("java.home");
    @NonConstant String property2 = System.getProperty("java.home", property);
    // :: error: assignment.type.incompatible
    @NonConstant String property3 = System.getProperty("java.home", "x");
  }

  // these examples are from https://docs.oracle.com/javase/8/docs/api/javax/swing/JOptionPane.html
  void testSwing() {
    @NonConstant String inputValue = JOptionPane.showInputDialog("Please input a value");

    Object[] possibleValues = { "First", "Second", "Third" };

    // :: error: assignment.type.incompatible
    @NonConstant Object selectedValue = JOptionPane.showInputDialog(null,
        "Choose one", "Input",
        JOptionPane.INFORMATION_MESSAGE, null,
        possibleValues, possibleValues[0]);
  }
}