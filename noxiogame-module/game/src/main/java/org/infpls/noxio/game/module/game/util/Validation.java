package org.infpls.noxio.game.module.game.util;

public class Validation {
  public static boolean isAlphaNumeric(final String in) {
    return in.matches("[a-zA-Z0-9]*");
  }
    public static boolean isAlphaNumericWithSpaces(final String in) {
    return in.matches("[a-zA-Z0-9 ]*");
  }
}
