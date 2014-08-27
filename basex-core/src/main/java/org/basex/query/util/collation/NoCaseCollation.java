package org.basex.query.util.collation;

import static org.basex.util.Token.*;

import java.util.*;

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
          char c1 = s1.charAt(s), c2 = s2.charAt(s);
          if(c1 != c2) {
            if(c1 >= 'a' && c1 <= 'z') c1 -= 0x20;
            if(c2 >= 'a' && c2 <= 'z') c2 -= 0x20;
            if(c1 != c2) return c1 - c2;
          }
        }
        return n1 - n2;
      }
    }, URL);
  }
}
