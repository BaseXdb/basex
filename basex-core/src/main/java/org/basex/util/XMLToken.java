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
  /** The underscore. */
  private static final byte[] UNDERSCORE = { '_' };

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

  /**
   * Encodes a string to a valid NCName.
   * @param name token
   * @param lax lax encoding (lossy, but better readable)
   * @return valid NCName
   */
  public static byte[] encode(final byte[] name, final boolean lax) {
    if(name.length == 0) return UNDERSCORE;

    for(int i = 0, cp; i < name.length; i += cl(name, i)) {
      cp = cp(name, i);
      if(cp == '_' || !(i == 0 ? XMLToken.isNCStartChar(cp) : XMLToken.isNCChar(cp))) {
        final TokenBuilder tb = new TokenBuilder(name.length << 1).add(name, 0, i);
        for(int j = i; j < name.length; j += cl(name, j)) {
          cp = cp(name, j);
          if(j == 0 ? XMLToken.isNCStartChar(cp) : XMLToken.isNCChar(cp)) {
            tb.add(cp);
          } else if(lax) {
            tb.add('_');
          } else if(cp == '_') {
            tb.add('_').add('_');
          } else if(cp < 0x10000) {
            addEsc(tb, cp);
          } else {
            final int r = cp - 0x10000;
            addEsc(tb, (r >>> 10) + 0xD800);
            addEsc(tb, (r & 0x3FF) + 0xDC00);
          }
        }
        return tb.finish();
      }
    }
    return name;
  }

  /**
   * Adds the given 16-bit char to the token builder in encoded form.
   * @param tb token builder
   * @param cp char
   */
  private static void addEsc(final TokenBuilder tb, final int cp) {
    tb.addByte(UNDERSCORE[0]);
    final int a = cp >>> 12;
    tb.addByte((byte) (a + (a > 9 ? 87 : '0')));
    final int b = cp >>> 8 & 0x0F;
    tb.addByte((byte) (b + (b > 9 ? 87 : '0')));
    final int c = cp >>> 4 & 0x0F;
    tb.addByte((byte) (c + (c > 9 ? 87 : '0')));
    final int d = cp & 0x0F;
    tb.addByte((byte) (d + (d > 9 ? 87 : '0')));
  }

  /**
   * Decodes an NCName to a string.
   * @param name name
   * @return cached QName
   */
  public static byte[] decode(final byte[] name) {
    // convert name to valid XML representation
    final TokenBuilder tb = new TokenBuilder();
    int uc = 0;
    // mode: 0=normal, 1=unicode, 2=underscore, 3=building unicode
    int mode = 0;
    for(int n = 0; n < name.length;) {
      final int cp = cp(name, n);
      if(mode >= 3) {
        uc = (uc << 4) + cp - (cp >= '0' && cp <= '9' ? '0' : 0x37);
        if(++mode == 7) {
          tb.add(uc);
          mode = 0;
          uc = 0;
        }
      } else if(cp == '_') {
        // limit underscore counter
        if(++mode == 3) {
          tb.add('_');
          mode = 0;
          continue;
        }
      } else if(mode == 1) {
        // unicode
        mode = 3;
        continue;
      } else if(mode == 2) {
        // underscore
        tb.add('_');
        mode = 0;
        continue;
      } else {
        // normal character
        tb.add(cp);
        mode = 0;
      }
      n += cl(name, n);
    }
    if(mode == 2) {
      tb.add('_');
    } else if(mode > 0 && !tb.isEmpty()) {
      tb.add('?');
    }
    return tb.finish();
  }
}
