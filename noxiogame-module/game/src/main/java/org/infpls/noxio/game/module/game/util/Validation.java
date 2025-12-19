package org.infpls.noxio.game.module.game.util;

public class Validation {
  public static boolean isAlphaNumeric(final String in) {
    return in.matches("[a-zA-Z0-9]*");
  }
  
  public static boolean isAlphaNumericWithSpaces(final String in) {
    return in.matches("[a-zA-Z0-9 _-]*");
  }
  
  public static boolean isMapName(final String in) {
    return in.matches("[a-zA-Z0-9 _-]*");
  }
  
  /* If the string is longer than <int l> the the string is clipped to that length */
  public static String clip(String s, int l) {
    if(s.length() > l) { return s.substring(l); }
    return s;
  }
}
