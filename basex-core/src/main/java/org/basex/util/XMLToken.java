package org.basex.util;

import static org.basex.util.Token.*;

import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * This class provides convenience operations for XML-specific character
 * operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XMLToken {
  /** Index for all HTML entities (lazy initialization). */
  private static TokenObjectMap<byte[]> entities;

  /** Hidden constructor. */
  private XMLToken() { }

  /**
   * Checks if the specified character is a valid XML 1.0 character.
   * @param cp codepoint of the character
   * @return result of check
   */
  public static boolean valid(final int cp) {
    return cp < 0xD800 ? cp >= 0x20 || cp == 0xA || cp == 0x9 || cp == 0xD :
      cp >= 0xE000 && cp <= 0xFFFD || cp >= 0x10000 && cp <= 0x10ffff;
  }

  /**
   * Checks if the specified character is a name start character, as required
   * e.g. by QName and NCName.
   * @param cp codepoint of the character
   * @return result of check
   */
  public static boolean isNCStartChar(final int cp) {
    return cp < 0x80 ?
      cp >= 'A' && cp <= 'Z' || cp >= 'a' && cp <= 'z' || cp == '_' :
      cp < 0x300 ? cp >= 0xC0 && cp != 0xD7 && cp != 0xF7 :
      cp >= 0x370 && cp <= 0x37D || cp >= 0x37F && cp <= 0x1FFF ||
      cp >= 0x200C && cp <= 0x200D || cp >= 0x2070 && cp <= 0x218F ||
      cp >= 0x2C00 && cp <= 0x2EFF || cp >= 0x3001 && cp <= 0xD7FF ||
      cp >= 0xF900 && cp <= 0xFDCF || cp >= 0xFDF0 && cp <= 0xFFFD ||
      cp >= 0x10000 && cp <= 0xEFFFF;
  }

  /**
   * Checks if the specified character is an XML letter.
   * @param cp codepoint of the character
   * @return result of check
   */
  public static boolean isNCChar(final int cp) {
    return isNCStartChar(cp) ||
      (cp < 0x100 ? digit(cp) || cp == '-' || cp == '.' || cp == 0xB7 :
      cp >= 0x300 && cp <= 0x36F || cp == 0x203F || cp == 0x2040);
  }

  /**
   * Checks if the specified character is an XML first-letter.
   * @param cp codepoint of the character
   * @return result of check
   */
  public static boolean isStartChar(final int cp) {
    return isNCStartChar(cp) || cp == ':';
  }

  /**
   * Checks if the specified character is an XML letter.
   * @param cp codepoint of the character
   * @return result of check
   */
  public static boolean isChar(final int cp) {
    return isNCChar(cp) || cp == ':';
  }

  /**
   * Checks if the specified token is a valid NCName.
   * @param value value to be checked
   * @return result of check
   */
  public static boolean isNCName(final byte[] value) {
    final int vl = value.length;
    return vl != 0 && ncName(value, 0) == vl;
  }

  /**
   * Checks if the specified token is a valid name.
   * @param value value to be checked
   * @return result of check
   */
  public static boolean isName(final byte[] value) {
    final int vl = value.length;
    for(int v = 0; v < vl; v += cl(value, v)) {
      final int cp = cp(value, v);
      if(v == 0 ? !isStartChar(cp) : !isChar(cp)) return false;
    }
    return vl != 0;
  }

  /**
   * Checks if the specified token is a valid NMToken.
   * @param value value to be checked
   * @return result of check
   */
  public static boolean isNMToken(final byte[] value) {
    final int vl = value.length;
    for(int v = 0; v < vl; v += cl(value, v)) {
      if(!isChar(cp(value, v))) return false;
    }
    return vl != 0;
  }

  /**
   * Checks if the specified token is a valid QName.
   * @param value value to be checked
   * @return result of check
   */
  public static boolean isQName(final byte[] value) {
    final int vl = value.length;
    if(vl == 0) return false;
    final int i = ncName(value, 0);
    if(i == vl) return true;
    if(i == 0 || value[i] != ':') return false;
    final int j = ncName(value, i + 1);
    return j == vl && j != i + 1;
  }

  /**
   * Checks the specified token as an NCName.
   * @param value value to be checked
   * @param start start position
   * @return end position
   */
  private static int ncName(final byte[] value, final int start) {
    final int vl = value.length;
    for(int v = start; v < vl; v += cl(value, v)) {
      final int cp = cp(value, v);
      if(v == start ? !isNCStartChar(cp) : !isNCChar(cp)) return v;
    }
    return vl;
  }

  /**
   * Checks if the specified name is an ID/IDREF attribute (IDREF: local name must contain 'idref';
   * ID: local name must contain 'id', but not 'idref').
   * The correct approach would be to gather all ID/IDREF attributes and store them as metadata.
   * @param name name
   * @param idref ID/IDREF flag
   * @return result of check
   */
  public static boolean isId(final byte[] name, final boolean idref) {
    final byte[] id = lc(local(name));
    return idref ? contains(id, REF) : contains(id, ID) && !contains(id, REF);
  }

  /**
   * Encodes a token to a valid NCName.
   * @param name token to be encoded
   * @param lax lax encoding (lossy, but better readable)
   * @return valid NCName
   */
  public static byte[] encode(final byte[] name, final boolean lax) {
    // lax encoding: trim whitespace
    final byte[] nm = lax ? trim(name) : name;
    final int nl = nm.length;
    if(nl == 0) return cpToken('_');

    for(int n = 0; n < nl; n += cl(nm, n)) {
      int cp = cp(nm, n);
      if(cp == '_' || (n == 0 ? !isNCStartChar(cp) : !isNCChar(cp))) {
        final TokenBuilder tb = new TokenBuilder((long) nl << 1).add(nm, 0, n);
        for(int m = n; m < nl; m += cl(nm, m)) {
          cp = cp(nm, m);
          if(lax) {
            final boolean nc = isNCChar(cp);
            // prefix invalid start chars (numbers, dashes, dots) with underscore
            if(m == 0 && nc && !isNCStartChar(cp)) tb.add('_');
            tb.add(nc ? cp : '_');
          } else if(cp == '_') {
            tb.add('_').add('_');
          } else if(m == 0 ? isNCStartChar(cp) : isNCChar(cp)) {
            tb.add(cp);
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
    return nm;
  }

  /**
   * Adds the given 16-bit char to the token builder in encoded form.
   * @param tb token builder
   * @param cp char
   */
  private static void addEsc(final TokenBuilder tb, final int cp) {
    tb.addByte((byte) '_');
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
   * Decodes an NCName to a token.
   * @param name name
   * @param lax lax decoding
   * @return token
   */
  public static byte[] decode(final byte[] name, final boolean lax) {
    final int nl = name.length;
    if(nl == 0) return null;
    if(lax) return name;

    // mode: 0=normal, 1=unicode, 2=underscore, 3=building unicode
    final TokenBuilder tb = new TokenBuilder();
    int mode = 0, uc = 0;
    for(int n = 0; n < nl;) {
      final int cp = cp(name, n);
      if(mode >= 3) {
        uc <<= 4;
        if(cp >= '0' && cp <= '9') {
          uc += cp - '0';
        } else if(cp >= 'A' && cp <= 'F') {
          uc += cp - 0x37;
        } else if(cp >= 'a' && cp <= 'f') {
          uc += cp - 0x57;
        } else {
          return null;
        }
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
        if(!(tb.isEmpty() ? isNCStartChar(cp) : isNCChar(cp))) return null;
        tb.add(cp);
        mode = 0;
      }
      n += cl(name, n);
    }

    if(mode == 2) {
      tb.add('_');
    } else if(mode == 1 && !tb.isEmpty() || mode >= 3) {
      return null;
    }
    return tb.finish();
  }

  /**
   * Returns a URI-decoded token.
   * @param token encoded token
   * @return decoded token
   */
  public static byte[] decodeUri(final byte[] token) {
    final int tl = token.length;
    final TokenBuilder tb = new TokenBuilder(tl);
    for(int t = 0; t < tl; t++) {
      int b = token[t];
      if(b == '+') {
        b = ' ';
      } else if(b == '%') {
        final int b1 = ++t < tl ? dec(token[t]) : -1, b2 = ++t < tl ? dec(token[t]) : -1;
        b = b1 != -1 && b2 != -1 ? b1 << 4 | b2 : -1;
      }
      if(b == -1) tb.add(Token.REPLACEMENT);
      else tb.addByte((byte) b);
    }

    Token.string(tb.next()).codePoints().forEach(cp -> {
      tb.add(XMLToken.valid(cp) ? cp : Token.REPLACEMENT);
    });
    return tb.finish();
  }

  /**
   * URI-decodes a string.
   * @param string encoded string
   * @return decoded string
   */
  public static String decodeUri(final String string) {
    return Token.string(decodeUri(Token.token(string)));
  }

  /**
   * Returns the unicode token for the specified entity or {@code null}.
   * @param key key
   * @return unicode token
   */
  public static byte[] getEntity(final byte[] key) {
    return entities().get(key);
  }

  /**
   * Returns the most similar entity.
   * @param key key
   * @return most similar entity or {@code null}
   */
  public static Object similarEntity(final byte[] key) {
    final TokenObjectMap<byte[]> map = entities();
    final TokenList list = new TokenList(map.size());
    for(final byte[] entity : map) list.add(entity);
    return Levenshtein.similar(key, list.finish());
  }

  /**
   * Returns the initialized entity map.
   * @return entity map
   */
  private static TokenObjectMap<byte[]> entities() {
    if(entities == null) entities = Util.properties("entities.properties");
    return entities;
  }
}
