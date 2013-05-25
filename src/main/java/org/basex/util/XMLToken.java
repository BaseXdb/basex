package org.basex.util;

import static org.basex.util.Token.*;

/**
 * This class provides convenience operations for XML-specific character
 * operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XMLToken {
  /** Hidden constructor. */
  private XMLToken() { }

  /**
   * Checks if the specified character is a valid XML 1.0 character.
   * @param ch the letter to be checked
   * @return result of check
   */
  public static boolean valid(final int ch) {
    return ch < 0xD800 ? ch >= 0x20 || ch == 0xA || ch == 0x9 || ch == 0xD :
      ch >= 0xE000 && ch <= 0xFFFD || ch >= 0x10000 && ch <= 0x10ffff;
  }

  /**
   * Checks if the specified character is a name start character, as required
   * e.g. by QName and NCName.
   * @param ch character
   * @return result of check
   */
  public static boolean isNCStartChar(final int ch) {
    return ch < 0x80 ?
      ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '_' :
      ch < 0x300 ? ch >= 0xC0 && ch != 0xD7 && ch != 0xF7 :
      ch >= 0x370 && ch <= 0x37D || ch >= 0x37F && ch <= 0x1FFF ||
      ch >= 0x200C && ch <= 0x200D || ch >= 0x2070 && ch <= 0x218F ||
      ch >= 0x2C00 && ch <= 0x2EFF || ch >= 0x3001 && ch <= 0xD7FF ||
      ch >= 0xF900 && ch <= 0xFDCF || ch >= 0xFDF0 && ch <= 0xFFFD ||
      ch >= 0x10000 && ch <= 0xEFFFF;
  }

  /**
   * Checks if the specified character is an XML letter.
   * @param ch character
   * @return result of check
   */
  public static boolean isNCChar(final int ch) {
    return isNCStartChar(ch) ||
      (ch < 0x100 ? digit(ch) || ch == '-' || ch == '.' || ch == 0xB7 :
      ch >= 0x300 && ch <= 0x36F || ch == 0x203F || ch == 0x2040);
  }

  /**
   * Checks if the specified character is an XML first-letter.
   * @param ch the letter to be checked
   * @return result of check
   */
  public static boolean isStartChar(final int ch) {
    return isNCStartChar(ch) || ch == ':';
  }

  /**
   * Checks if the specified character is an XML letter.
   * @param ch the letter to be checked
   * @return result of check
   */
  public static boolean isChar(final int ch) {
    return isNCChar(ch) || ch == ':';
  }

  /**
   * Checks if the specified token is a valid NCName.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isNCName(final byte[] v) {
    final int l = v.length;
    return l != 0 && ncName(v, 0) == l;
  }

  /**
   * Checks if the specified token is a valid name.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isName(final byte[] v) {
    final int l = v.length;
    for(int i = 0; i < l; i += cl(v, i)) {
      final int c = cp(v, i);
      if(i == 0 ? !isStartChar(c) : !isChar(c)) return false;
    }
    return l != 0;
  }

  /**
   * Checks if the specified token is a valid NMToken.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isNMToken(final byte[] v) {
    final int l = v.length;
    for(int i = 0; i < l; i += cl(v, i)) if(!isChar(cp(v, i))) return false;
    return l != 0;
  }

  /**
   * Checks if the specified token is a valid EQName.
   * @param val value to be checked
   * @return result of check
   */
  public static boolean isEQName(final byte[] val) {
    final int l = val.length;
    if(l == 0) return false;
    if(l < 2 || val[0] != 'Q' || val[1] != '{') return isQName(val);
    final int i = indexOf(val, '}');
    if(i == -1 || i == l) return false;
    final int j = ncName(val, i + 1);
    return j == l && j != i + 1;
  }

  /**
   * Checks if the specified token is a valid QName.
   * @param val value to be checked
   * @return result of check
   */
  public static boolean isQName(final byte[] val) {
    final int l = val.length;
    if(l == 0) return false;
    final int i = ncName(val, 0);
    if(i == l) return true;
    if(i == 0 || val[i] != ':') return false;
    final int j = ncName(val, i + 1);
    return j == l && j != i + 1;
  }

  /**
   * Checks the specified token as an NCName.
   * @param v value to be checked
   * @param p start position
   * @return end position
   */
  private static int ncName(final byte[] v, final int p) {
    final int l = v.length;
    for(int i = p; i < l; i += cl(v, i)) {
      final int c = cp(v, i);
      if(i == p ? !isNCStartChar(c) : !isNCChar(c)) return i;
    }
    return l;
  }
}
