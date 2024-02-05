package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnChar extends StandardFunc {
  @Override
  public final Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).item(qc, info);

    // codepoint integer
    if(value instanceof Int) return finish(toLong(value, 1), value);

    // codepoint string
    final byte[] token = toToken(value);
    if(Token.startsWith(token, '#')) return finish(parseCodepoint(token), value);

    final int nl = token.length;
    if(nl == 2 && token[0] == '\\') {
      // backslash-escape sequence
      final int cp = token[1];
      if(cp == 't') return Str.TAB;
      if(cp == 'n') return Str.NL;
      if(cp == 'r') return Str.CR;
    } else if(nl > 0) {
      // HTML character reference name
      final byte[] result = XMLToken.getEntity(token);
      if(result != null) return Str.get(result);
      throw CHARINV_X.get(info, QueryError.similar(token, XMLToken.similarEntity(token)));
    }

    throw CHARINV_X.get(info, token);
  }

  /**
   * Parses a decimal or hexadecimal codepoint value.
   * @param token token
   * @return value, or {@code -1} if the token cannot be parsed
   */
  private static long parseCodepoint(final byte[] token) {
    final int nl = token.length;
    if(nl < 2) return -1;
    int cp = 0;
    final boolean dec = token[1] != 'x';
    for(int i = dec ? 1 : 2; i < nl; i++) {
      final int d = Token.uc(token[i]);
      if(d < '0' || d > '9' && (dec || d < 'A' || d > 'F')) return -1;
      cp = cp * (dec ? 10 : 16) + d - (d <= '9' ? 0x30 : 0x37);
    }
    return cp;
  }

  /**
   * Converts a codepoint to a string.
   * @param cp codepoint
   * @param item input item
   * @return token or {@code null}
   * @throws QueryException query exception
   */
  private Str finish(final long cp, final Item item) throws QueryException {
    if(cp >= 0 && cp <= Integer.MAX_VALUE && XMLToken.valid((int) cp)) {
      return Str.get(new TokenBuilder().add((int) cp).finish());
    }
    throw CHARINV_X.get(info, item);
  }
}
