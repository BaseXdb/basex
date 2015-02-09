package org.basex.query.util.collation;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Case-insensitive collation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class NoCaseCollation extends Collation {
  @Override
  public int compare(final byte[] string, final byte[] compare) {
    final String str = string(string), cmp = string(compare);
    final int tl = str.length(), cl = cmp.length(), l = Math.min(tl, cl);
    for(int i = 0; i < l; ++i) {
      final int c = diff(str.charAt(i), cmp.charAt(i));
      if(c != 0) return c;
    }
    return tl - cl;
  }

  @Override
  protected int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo info) throws QueryException {

    final int tl = string.length(), sl = sub.length();
    if(sl == 0) return 0;
    if(tl < sl) return -1;

    for(int t = mode == Mode.ENDS_WITH ? tl - sl : 0; t < tl; ++t) {
      int s = 0;
      while(diff(string.charAt(t + s), sub.charAt(s)) == 0) {
        if(++s == sl) return mode == Mode.INDEX_AFTER ? t + s : t;
      }
      if(mode == Mode.STARTS_WITH) return -1;
    }
    return -1;
  }

  /**
   * Compares two characters.
   * @param ch1 first character
   * @param ch2 second character
   * @return difference
   */
  private static int diff(final char ch1, final char ch2) {
    if(ch1 != ch2) {
      final int c1 = ch1 >= 'a' && ch1 <= 'z' ? ch1 - 0x20 : ch1;
      final int c2 = ch2 >= 'a' && ch2 <= 'z' ? ch2 - 0x20 : ch2;
      if(c1 != c2) return c1 - c2;
    }
    return 0;
  }
}
