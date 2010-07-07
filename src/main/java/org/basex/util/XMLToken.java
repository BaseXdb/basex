package org.basex.util;

import static org.basex.util.Token.*;

/**
 * This class provides convenience operations for XML-specific character
 * operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class XMLToken {
  /** Hidden constructor. */
  private XMLToken() { }

  /**
   * Checks if the specified character is a valid XML character.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean valid(final long ch) {
    return ch >= 0x20 && ch <= 0xD7FF || ch == 0xA || ch == 0x9 || ch == 0xD ||
      ch >= 0xE000 && ch <= 0xFFFD || ch >= 0x10000 && ch <= 0x10ffff;
  }

  /**
   * Checks if the specified character is an XML letter.
   * @param ch character
   * @return result of check
   */
  public static boolean isXMLLetter(final int ch) {
    return ch == '_' || Character.isLetter(ch);
  }

  /**
   * Checks if the specified character is an XML letter.
   * @param ch character
   * @return result of check
   */
  public static boolean isXMLLetterOrDigit(final int ch) {
    return isXMLLetter(ch) || digit(ch);
  }

  /**
   * Checks if the specified character is an XML first-letter.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isFirstLetter(final int ch) {
    return isXMLLetter(ch) || ch == ':';
  }

  /**
   * Checks if the specified character is an XML letter.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isLetterOrDigit(final int ch) {
    return isFirstLetter(ch) || digit(ch) || ch == '-' || ch == '.';
  }

  /**
   * Checks if the specified token is a valid NCName.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isNCName(final byte[] v) {
    final int l = v.length;
    return l == 0 ? false : ncName(v, -1) == l;
  }

  /**
   * Checks the specified token as an NCName.
   * @param v value to be checked
   * @param p start position minus 1
   * @return end position
   */
  private static int ncName(final byte[] v, final int p) {
    final int l = v.length;
    for(int i = p + 1; i < l; i += cl(v, i)) {
      final int c = cp(v, i);
      if(isXMLLetter(c)) continue;
      if(i == p + 1 || !digit(c) && c != '-' && c != '.') return i;
    }
    return l;
  }

  /**
   * Checks if the specified token is a valid name.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isName(final byte[] v) {
    if(v.length == 0 || !isFirstLetter(v[0])) return false;
    for(int i = 1; i < v.length; i++) if(!isLetterOrDigit(v[i])) return false;
    return true;
  }

  /**
   * Checks if the specified token is a valid NMToken.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isNMToken(final byte[] v) {
    if(v.length == 0) return false;
    for(final byte c : v) if(!isLetterOrDigit(c)) return false;
    return true;
  }

  /**
   * Checks if the specified token is a valid QName.
   * @param val value to be checked
   * @return result of check
   */
  public static boolean isQName(final byte[] val) {
    final int l = val.length;
    if(l == 0) return false;
    final int i = ncName(val, -1);
    if(i == l) return true;
    if(i == 0 || val[i] != ':') return false;
    final int j = ncName(val, i);
    if(j == i + 1 || j != l) return false;
    return true;
  }
}
