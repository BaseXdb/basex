package org.basex.util;

/**
 * This class provides convenience operations for smart string comparisons.
 * If query strings and character are lower case, search will be case insensitive.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SmartStrings {
  /** Hidden constructor. */
  private SmartStrings() { }

  /**
   * Checks if a string starts with the specified substring.
   * @param string string
   * @param sub substring
   * @return result of check
   */
  public static boolean startsWith(final String string, final String sub) {
    final int sl = string.length(), tl = sub.length();
    if(tl > sl) return false;
    for(int t = 0; t < tl; t++) {
      if(!equals(string.charAt(t), sub.charAt(t))) return false;
    }
    return true;
  }

  /**
   * Checks if a string contains the specified substring.
   * @param string string
   * @param sub substring
   * @return result of check
   */
  public static boolean contains(final String string, final String sub) {
    final int tl = sub.length();
    if(tl == 0) return true;
    final int sl = string.length() - tl;
    for(int s = 0; s <= sl; s++) {
      int t = 0;
      while(equals(string.charAt(s + t), sub.charAt(t))) {
        if(++t == tl) return true;
      }
    }
    return false;
  }

  /**
   * Checks if all characters of the specified substring occur in a string in the given order.
   * @param string string
   * @param sub substring
   * @return result of check
   */
  public static boolean matches(final String string, final String sub) {
    final int sl = string.length(), tl = sub.length();
    int t = 0;
    for(int s = 0; s < sl && t < tl; s++) {
      if(equals(string.charAt(s), sub.charAt(t))) t++;
    }
    return t == tl;
  }

  /**
   * Compares two characters.
   * @param input input character
   * @param query query character
   * @return result of check
   */
  private static boolean equals(final char input, final char query) {
    return query == (Character.isUpperCase(query) ? input : Character.toLowerCase(input));
  }
}
