package org.basex.query.util.collation;

import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Case-insensitive collation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class HtmlNoCaseCollation extends Collation {
  /** Collation URI. */
  static final byte[] URI = Token.concat(QueryText.FN_URI,
      "/collation/html-ascii-case-insensitive");
  /** Singleton instance. */
  static final HtmlNoCaseCollation INSTANCE = new HtmlNoCaseCollation();

  /** Private constructor. */
  private HtmlNoCaseCollation() {
    super(URI);
  }

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    final int sl = string.length, cl = compare.length, l = Math.min(sl, cl);
    for(int i = 0; i < l; i++) {
      final int d = Integer.signum(lc(string[i] & 0xFF) - lc(compare[i] & 0xFF));
      if(d != 0) return d;
    }
    return Integer.signum(sl - cl);
  }

  @Override
  CollationElements elements(final String string, final InputInfo info) {
    final int[] pos = { 0 };
    return new CollationElements(
      () -> pos[0] < string.length() ? lc(string.charAt(pos[0]++)) : -1, () -> pos[0]);
  }

  @Override
  public byte[] key(final byte[] string, final InputInfo info) {
    final ByteList bl = new ByteList(string.length);
    for(final byte b : string) bl.add(lc(b));
    return key(bl.finish());
  }

  /**
   * Converts an ASCII character to lower case.
   * @param cp codepoint
   * @return lower-case representation
   */
  private static int lc(final int cp) {
    return cp >= 'A' && cp <= 'Z' ? cp + 0x20 : cp;
  }
}
