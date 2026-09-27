package org.basex.query.util.collation;

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
    return Token.compare(Token.lc(string), Token.lc(compare));
  }

  @Override
  protected int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo ii) {

    // each codepoint is a collation unit; offsets refer to the original string
    final int[] stringCps = string.codePoints().toArray();
    final int[] subCps = sub.codePoints().toArray();
    final int tl = stringCps.length, sl = subCps.length;
    if(sl == 0) return mode.last() ? string.length() : 0;
    int last = -1;
    if(tl >= sl) {
      for(int t = mode == Mode.ENDS_WITH ? tl - sl : 0; t < tl; t++) {
        for(int s = 0; t + s < tl;) {
          if(compare(stringCps[t + s], subCps[s]) != 0) break;
          if(++s == sl) {
            last = mode.after() ? t + s : t;
            if(!mode.last()) return string.offsetByCodePoints(0, last);
            break;
          }
        }
        if(mode == Mode.STARTS_WITH) return -1;
      }
    }
    return last == -1 ? -1 : string.offsetByCodePoints(0, last);
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
