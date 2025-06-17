package org.basex.query.util.collation;

import java.util.*;

import org.basex.util.*;

/**
 * Case-insensitive collation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class UnicodeNoCaseCollation extends Collation {
  /** Singleton instance. */
  static final UnicodeNoCaseCollation INSTANCE = new UnicodeNoCaseCollation();

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    final int sl = string.length, cl = compare.length, l = Math.min(sl, cl);
    for(int s = 0, c = 0; s < l || c < l; s += Token.cl(string, s), c += Token.cl(compare, c)) {
      final int cp1 = Token.cp(string, s), cp2 = Token.cp(compare, s);
      final int d = compare(lc(cp1), lc(cp2));
      if(d != 0) return d;
    }
    return Integer.signum(sl - cl);
  }

  @Override
  protected int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo ii) {

    final int[] stringCps = string.toLowerCase(Locale.ENGLISH).codePoints().toArray();
    final int[] subCps = sub.toLowerCase(Locale.ENGLISH).codePoints().toArray();
    final int tl = stringCps.length, sl = subCps.length;
    if(sl == 0) return 0;
    if(tl >= sl) {
      for(int t = mode == Mode.ENDS_WITH ? tl - sl : 0; t < tl; ++t) {
        for(int s = 0; t + s < tl;) {
          if(compare(stringCps[t + s], subCps[s]) != 0) break;
          if(++s == sl) return mode == Mode.INDEX_AFTER ? t + s : t;
        }
        if(mode == Mode.STARTS_WITH) return -1;
      }
    }
    return -1;
  }

  @Override
  public byte[] key(final byte[] string, final InputInfo info) {
    return Collation.key(Token.lc(string));
  }

  /**
   * Compares two codepoints.
   * @param cp1 first codepoint
   * @param cp2 second codepoint
   * @return result of comparison (-1, 0, 1)
   */
  private static int compare(final int cp1, final int cp2) {
    return Integer.signum(lc(cp1) - lc(cp2));
  }

  /**
   * Converts an ASCII character to lower case.
   * @param cp codepoint
   * @return lower-case representation
   */
  private static int lc(final int cp) {
    return Character.toLowerCase(cp);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof UnicodeNoCaseCollation;
  }
}
