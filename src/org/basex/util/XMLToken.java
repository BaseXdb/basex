package org.basex.util;

import static org.basex.util.Token.*;
import org.basex.build.xml.XMLScanner;

/**
 * This class provides convenience operations for XML-specific character
 * operations. This class is mainly called by the {@link XMLScanner} and
 * XQuery methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XMLToken {
  /** Hidden Constructor. */
  private XMLToken() { }

  /**
   * Checks if the specified character is a valid XML character.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean valid(final int ch) {
    return ch >= 0x20 && ch <= 0xD7FF || ch == 0xA || ch == 0x9 || ch == 0xD ||
      ch >= 0xE000 && ch <= 0xFFFD || ch >= 0x10000 && ch <= 0x10ffff;
  }

  /**
   * Checks if the specified character is an XML first-letter.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isFirstLetter(final int ch) {
    // [CG] XML/Scanning: Unicode support
    return letter(ch) || ch == ':' || ch < 0;
  }
  
  /**
   * Checks if the specified character is an XML letter.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isLetter(final int ch) {
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
   * @param p start position
   * @return end position
   */
  public static int ncName(final byte[] v, final int p) {
    int i = p;
    while(++i < v.length) {
      final byte c = v[i];
      if(letter(c)) continue;
      if(i == p + 1 || !digit(c) && c != '-' && c != '.') return i;
    }
    return i;
  }
  
  /**
   * Checks if the specified token is a valid Name.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isName(final byte[] v) {
    if(v.length == 0 || !isFirstLetter(v[0])) return false;
    for(int i = 1; i < v.length; i++) if(!isLetter(v[i])) return false;
    return true;
  }

  /**
   * Checks if the specified token is a valid NMToken.
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isNMToken(final byte[] v) {
    if(v.length == 0) return false;
    for(byte c : v) if(!isLetter(c)) return false;
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
