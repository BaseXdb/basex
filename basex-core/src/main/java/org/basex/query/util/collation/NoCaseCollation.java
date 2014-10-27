package org.basex.query.util.collation;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Case-insensitive collation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class NoCaseCollation extends Collation {
  /** Case-insensitive collation. */
  static final byte[] URL =
      token("http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive");

  /** Private constructor. */
  private static final NoCaseCollation INSTANCE = new NoCaseCollation();

  /**
   * Returns a singleton instance of this class.
   * @return instance
   */
  static NoCaseCollation get() {
    return INSTANCE;
  }

  @Override
  protected int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo info) throws QueryException {

    final int tl = string.length(), sl = sub.length();
    if(sl == 0) return 0;
    if(tl < sl) return -1;

    for(int t = mode == Mode.ENDS_WITH ? tl - sl : 0; t < tl; ++t) {
      int s = 0;
      while(comp(string.charAt(t + s), sub.charAt(s)) == 0) {
        if(++s == sl) return mode == Mode.INDEX_AFTER ? t + s : t;
      }
      if(mode == Mode.STARTS_WITH) return -1;
    }
    return -1;
  }

  /** Private Constructor. */
  @SuppressWarnings("rawtypes")
  private NoCaseCollation() {
    super(new Comparator() {
      @Override
      public int compare(final Object o1, final Object o2) {
        final String s1 = (String) o1, s2 = (String) o2;
        final int n1 = s1.length(), n2 = s2.length();
        final int sl = Math.min(n1, n2);
        for(int s = 0; s < sl; s++) {
          final int d = comp(s1.charAt(s), s2.charAt(s));
          if(d != 0) return d;
        }
        return n1 - n2;
      }
    }, URL);
  }

  /**
   * Compares two characters.
   * @param ch1 first character
   * @param ch2 second character
   * @return difference
   */
  private static int comp(final char ch1, final char ch2) {
    if(ch1 != ch2) {
      final int c1 = ch1 >= 'a' && ch1 <= 'z' ? ch1 - 0x20 : ch1;
      final int c2 = ch2 >= 'a' && ch2 <= 'z' ? ch2 - 0x20 : ch2;
      if(c1 != c2) return c1 - c2;
    }
    return 0;
  }
}
