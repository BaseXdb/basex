package org.basex.query.util.collation;

import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Case-insensitive collation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class UnicodeNoCaseCollation extends Collation {
  /** Collation URI. */
  static final byte[] URI = Token.concat(QueryText.FN_URI, "/collation/unicode-case-insensitive");
  /** Singleton instance. */
  static final UnicodeNoCaseCollation INSTANCE = new UnicodeNoCaseCollation();

  /** Private constructor. */
  private UnicodeNoCaseCollation() {
    super(URI);
  }

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    return Token.compare(Token.lc(string), Token.lc(compare));
  }

  @Override
  CollationElements elements(final String string, final InputInfo info) {
    // the codepoints of the lower-case string are the collation units, as in compare and key
    return new CollationElements(string, s -> s.toLowerCase(Locale.ENGLISH));
  }

  @Override
  public byte[] key(final byte[] string, final InputInfo info) {
    return Collation.key(Token.lc(string));
  }
}
