package org.basex.util;

import static org.basex.util.Token.*;
import org.basex.util.hash.*;

/**
 * This class provides convenience operations for XML-specific character
 * operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XMLToken {
  /** Index for all HTML entities (lazy initialization). */
  private static final TokenMap HTMLENTS = new TokenMap();
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
      final int c = cp(value, i);
      if(i == 0 ? !isStartChar(c) : !isChar(c)) return false;
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
    for(int i = 0; i < l; i += cl(value, i)) if(!isChar(cp(value, i))) return false;
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
   * Encodes a string to a valid NCName.
   * @param name token to be encoded
   * @param lax lax encoding (lossy, but better readable)
   * @return valid NCName
   */
  public static byte[] encode(final byte[] name, final boolean lax) {
    // lax encoding: trim whitespaces
    final byte[] nm = lax ? trim(name) : name;
    if(nm.length == 0) return UNDERSCORE;

    for(int i = 0; i < nm.length; i += cl(nm, i)) {
      int cp = cp(nm, i);
      if(cp == '_' || !(i == 0 ? isNCStartChar(cp) : isNCChar(cp))) {
        final TokenBuilder tb = new TokenBuilder(nm.length << 1).add(nm, 0, i);
        for(int j = i; j < nm.length; j += cl(nm, j)) {
          cp = cp(nm, j);
          if(lax) {
            final boolean nc = isNCChar(cp);
            // prefix invalid start chars (numbers, dashes, dots) with underscore
            if(j == 0 && nc && !isNCStartChar(cp)) tb.add('_');
            tb.add(nc ? cp : '_');
          } else if(cp == '_') {
            tb.add('_').add('_');
          } else if(j == 0 ? isNCStartChar(cp) : isNCChar(cp)) {
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
   * Decodes an NCName to a string.
   * @param name name
   * @param lax lax decoding
   * @return cached QName
   */
  public static byte[] decode(final byte[] name, final boolean lax) {
    if(lax) return name;

    // convert name back to original representation
    final TokenBuilder tb = new TokenBuilder();
    int uc = 0;

    // mode: 0=normal, 1=unicode, 2=underscore, 3=building unicode
    int mode = 0;
    for(int n = 0; n < name.length;) {
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
        tb.add(cp);
        mode = 0;
      }
      n += cl(name, n);
    }

    if(mode == 2) {
      tb.add('_');
    } else if(mode > 0 && !tb.isEmpty()) {
      return null;
    }
    return tb.finish();
  }

  /** HTML entities. */
  private static final String[] HTMLENTITIES = { "Aacute", "\u00c1", "aacute",
    "\u00e1", "Acirc", "\u00c2", "acirc", "\u00e2", "acute", "\u00b4",
    "AElig", "\u00c6", "aelig", "\u00e6", "Agrave", "\u00c0", "agrave",
    "\u00e0", "alefsym", "\u2135", "Alpha", "\u0391", "alpha", "\u03b1",
    "and", "\u2227", "ang", "\u2220", "Aring", "\u00c5", "aring", "\u00e5",
    "asymp", "\u2248", "Atilde", "\u00c3", "atilde", "\u00e3", "Auml",
    "\u00c4", "auml", "\u00e4", "bdquo", "\u201e", "Beta", "\u0392", "beta",
    "\u03b2", "brvbar", "\u00a6", "bull", "\u2022", "cap", "\u2229",
    "Ccedil", "\u00c7", "ccedil", "\u00e7", "cedil", "\u00b8", "cent",
    "\u00a2", "Chi", "\u03a7", "chi", "\u03c7", "circ", "\u02c6", "clubs",
    "\u2663", "cong", "\u2245", "copy", "\u00a9", "crarr", "\u21b5", "cup",
    "\u222a", "curren", "\u00a4", "dagger", "\u2020", "Dagger", "\u2021",
    "darr", "\u2193", "dArr", "\u21d3", "deg", "\u00b0", "Delta", "\u0394",
    "delta", "\u03b4", "diams", "\u2666", "divide", "\u00f7", "Eacute",
    "\u00c9", "eacute", "\u00e9", "Ecirc", "\u00ca", "ecirc", "\u00ea",
    "Egrave", "\u00c8", "egrave", "\u00e8", "empty", "\u2205", "emsp",
    "\u2003", "ensp", "\u2002", "Epsilon", "\u0395", "epsilon", "\u03b5",
    "equiv", "\u2261", "Eta", "\u0397", "eta", "\u03b7", "ETH", "\u00d0",
    "eth", "\u00f0", "Euml", "\u00cb", "euml", "\u00eb", "euro", "\u20ac",
    "exist", "\u2203", "fnof", "\u0192", "forall", "\u2200", "frac12",
    "\u00bd", "frac14", "\u00bc", "frac34", "\u00be", "frasl", "\u2044",
    "Gamma", "\u0393", "gamma", "\u03b3", "ge", "\u2265", "harr", "\u2194",
    "hArr", "\u21d4", "hearts", "\u2665", "hellip", "\u2026", "Iacute",
    "\u00cd", "iacute", "\u00ed", "Icirc", "\u00ce", "icirc", "\u00ee",
    "iexcl", "\u00a1", "Igrave", "\u00cc", "igrave", "\u00ec", "image",
    "\u2111", "infin", "\u221e", "int", "\u222b", "Iota", "\u0399", "iota",
    "\u03b9", "iquest", "\u00bf", "isin", "\u2208", "Iuml", "\u00cf", "iuml",
    "\u00ef", "Kappa", "\u039a", "kappa", "\u03ba", "Lambda", "\u039b",
    "lambda", "\u03bb", "lang", "\u2329", "laquo", "\u00ab", "larr",
    "\u2190", "lArr", "\u21d0", "lceil", "\u2308", "ldquo", "\u201c", "le",
    "\u2264", "lfloor", "\u230a", "lowast", "\u2217", "loz", "\u25ca", "lrm",
    "\u200e", "lsaquo", "\u2039", "lsquo", "\u2018", "macr", "\u00af",
    "mdash", "\u2014", "micro", "\u00b5", "middot", "\u00b7", "minus",
    "\u2212", "Mu", "\u039c", "mu", "\u03bc", "nabla", "\u2207", "nbsp",
    "\u00a0", "ndash", "\u2013", "ne", "\u2260", "ni", "\u220b", "not",
    "\u00ac", "notin", "\u2209", "nsub", "\u2284", "Ntilde", "\u00d1",
    "ntilde", "\u00f1", "Nu", "\u039d", "nu", "\u03bd", "Oacute", "\u00d3",
    "oacute", "\u00f3", "Ocirc", "\u00d4", "ocirc", "\u00f4", "OElig",
    "\u0152", "oelig", "\u0153", "Ograve", "\u00d2", "ograve", "\u00f2",
    "oline", "\u203e", "Omega", "\u03a9", "omega", "\u03c9", "Omicron",
    "\u039f", "omicron", "\u03bf", "oplus", "\u2295", "or", "\u2228", "ordf",
    "\u00aa", "ordm", "\u00ba", "Oslash", "\u00d8", "oslash", "\u00f8",
    "Otilde", "\u00d5", "otilde", "\u00f5", "otimes", "\u2297", "Ouml",
    "\u00d6", "ouml", "\u00f6", "para", "\u00b6", "part", "\u2202", "permil",
    "\u2030", "perp", "\u22a5", "Phi", "\u03a6", "phi", "\u03c6", "Pi",
    "\u03a0", "pi", "\u03c0", "piv", "\u03d6", "plusmn", "\u00b1", "pound",
    "\u00a3", "prime", "\u2032", "Prime", "\u2033", "prod", "\u220f", "prop",
    "\u221d", "Psi", "\u03a8", "psi", "\u03c8", "radic", "\u221a", "rang",
    "\u232a", "raquo", "\u00bb", "rarr", "\u2192", "rArr", "\u21d2", "rceil",
    "\u2309", "rdquo", "\u201d", "real", "\u211c", "reg", "\u00ae", "rfloor",
    "\u230b", "Rho", "\u03a1", "rho", "\u03c1", "rlm", "\u200f", "rsaquo",
    "\u203a", "rsquo", "\u2019", "sbquo", "\u201a", "Scaron", "\u0160",
    "scaron", "\u0161", "sdot", "\u22c5", "sect", "\u00a7", "shy", "\u00ad",
    "Sigma", "\u03a3", "sigma", "\u03c3", "sigmaf", "\u03c2", "sim",
    "\u223c", "spades", "\u2660", "sub", "\u2282", "sube", "\u2286", "sum",
    "\u2211", "sup", "\u2283", "sup1", "\u00b9", "sup2", "\u00b2", "sup3",
    "\u00b3", "supe", "\u2287", "szlig", "\u00df", "Tau", "\u03a4", "tau",
    "\u03c4", "there4", "\u2234", "Theta", "\u0398", "theta", "\u03b8",
    "thetasym", "\u03d1", "thinsp", "\u2009", "THORN", "\u00de", "thorn",
    "\u00fe", "tilde", "\u02dc", "times", "\u00d7", "trade", "\u2122",
    "Uacute", "\u00da", "uacute", "\u00fa", "uarr", "\u2191", "uArr",
    "\u21d1", "Ucirc", "\u00db", "ucirc", "\u00fb", "Ugrave", "\u00d9",
    "ugrave", "\u00f9", "uml", "\u00a8", "upsih", "\u03d2", "Upsilon",
    "\u03a5", "upsilon", "\u03c5", "Uuml", "\u00dc", "uuml", "\u00fc",
    "weierp", "\u2118", "Xi", "\u039e", "xi", "\u03be", "Yacute", "\u00dd",
    "yacute", "\u00fd", "yen", "\u00a5", "yuml", "\u00ff", "Yuml", "\u0178",
    "Zeta", "\u0396", "zeta", "\u03b6", "zwj", "\u200d", "zwnj", "\u200c" };

  /**
   * Returns the unicode for the specified entity, or {@code null}.
   * @param key key
   * @return unicode
   */
  public static byte[] getEntity(final byte[] key) {
    if(HTMLENTS.isEmpty()) {
      for(int s = 0; s < HTMLENTITIES.length; s += 2) {
        HTMLENTS.put(HTMLENTITIES[s], HTMLENTITIES[s + 1]);
      }
    }
    return HTMLENTS.get(key);
  }
}
