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

    if(value instanceof Int) {
      // codepoint integer
      final long cp = toLong(value, 1);
      if(cp >= 0 && cp <= Integer.MAX_VALUE && XMLToken.valid((int) cp)) {
        return Str.get(new TokenBuilder().add((int) cp).finish());
      }
    } else {
      // codepoint string
      final byte[] token = toToken(value);
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
    }
    throw CHARINV_X.get(info, value);
  }
}
