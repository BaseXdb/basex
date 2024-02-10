package org.basex.util;

import static org.basex.util.Token.*;

import java.nio.charset.*;

import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * This class provides convenience operations for XML-specific character
 * operations.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class XMLToken {
  /** Index for all HTML entities (lazy initialization). */
  private static TokenMap entities;
  /** The underscore. */
  private static final byte[] UNDERSCORE = { '_' };

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
    final int l = value.length;
    return l != 0 && ncName(value, 0) == l;
  }

  /**
   * Checks if the specified token is a valid name.
   * @param value value to be checked
   * @return result of check
   */
  public static boolean isName(final byte[] value) {
    final int l = value.length;
    for(int i = 0; i < l; i += cl(value, i)) {
      final int cp = cp(value, i);
      if(i == 0 ? !isStartChar(cp) : !isChar(cp)) return false;
    }
    return l != 0;
  }

  /**
   * Checks if the specified token is a valid NMToken.
   * @param value value to be checked
   * @return result of check
   */
  public static boolean isNMToken(final byte[] value) {
    final int l = value.length;
    for(int i = 0; i < l; i += cl(value, i)) {
      if(!isChar(cp(value, i))) return false;
    }
    return l != 0;
  }

  /**
   * Checks if the specified token is a valid QName.
   * @param value value to be checked
   * @return result of check
   */
  public static boolean isQName(final byte[] value) {
    final int l = value.length;
    if(l == 0) return false;
    final int i = ncName(value, 0);
    if(i == l) return true;
    if(i == 0 || value[i] != ':') return false;
    final int j = ncName(value, i + 1);
    return j == l && j != i + 1;
  }

  /**
   * Checks the specified token as an NCName.
   * @param value value to be checked
   * @param start start position
   * @return end position
   */
  private static int ncName(final byte[] value, final int start) {
    final int l = value.length;
    for(int i = start; i < l; i += cl(value, i)) {
      final int c = cp(value, i);
      if(i == start ? !isNCStartChar(c) : !isNCChar(c)) return i;
    }
    return l;
  }

  /**
   * Checks if the specified name is an id/idref attribute ({@code idref}: local name must contain
   * 'idref'; {@code id}: local name must contain 'if', but not 'idref').
   * The correct approach would be to gather all id/idref attributes and store them as metadata.
   * @param name name
   * @param idref id/idref flag
   * @return result of check
   */
  public static boolean isId(final byte[] name, final boolean idref) {
    final byte[] n = lc(local(name));
    return idref ? contains(n, REF) : contains(n, ID) && !contains(n, REF);
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
    if(nl == 0) return UNDERSCORE;

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
   * Checks if the specified token contains invalid XML 1.0 characters.
   * @param token the token to be checked
   * @return invalid character or {@code -1}
   */
  public static int invalid(final byte[] token) {
    final TokenParser tp = new TokenParser(token);
    while(tp.more()) {
      final int cp = tp.next();
      if(!XMLToken.valid(cp)) return cp;
    }
    return -1;
  }

  /**
   * Returns a URI-decoded token.
   * @param token encoded token
   * @param plus decode '+' character
   * @return decoded token
   */
  public static byte[] decodeUri(final byte[] token, final boolean plus) {
    final int tl = token.length;
    final TokenBuilder tb = new TokenBuilder(tl);
    for(int t = 0; t < tl; t++) {
      int b = token[t];
      if(plus && b == '+') {
        b = ' ';
      } else if(b == '%') {
        final int b1 = ++t < tl ? dec(token[t]) : -1, b2 = ++t < tl ? dec(token[t]) : -1;
        b = b1 != -1 && b2 != -1 ? b1 << 4 | b2 : -1;
      }
      if(b == -1) tb.add(Token.REPLACEMENT);
      else tb.addByte((byte) b);
    }

    final byte[] decoded = Token.token(new String(tb.toArray(), StandardCharsets.UTF_8));
    tb.reset();
    final int dl = decoded.length;
    for(int d = 0; d < dl; d += cl(decoded, d)) {
      final int cp = cp(decoded, d);
      tb.add(XMLToken.valid(cp) ? cp : Token.REPLACEMENT);
    }
    return tb.finish();
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
    final TokenMap map = entities();
    final TokenList list = new TokenList(map.size());
    for(final byte[] entity : map) list.add(entity);
    return Levenshtein.similar(key, list.finish());
  }

  /**
   * Returns the initialized entity map.
   * @return entity map
   */
  private static TokenMap entities() {
    if(entities == null) entities = Util.properties("entities.properties");
    return entities;
  }
}
