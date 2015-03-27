package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class WebCreateUrl extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder().add(toToken(exprs[0], qc));
    final Map map = toMap(exprs[1], qc);
    int c = 0;
    for(final Item key : map.keys()) {
      final byte[] name = key.string(ii);
      for(final Item value : map.get(key, info)) {
        tb.add(c++ == 0 ? '?' : '&');
        tb.add(Token.uri(name, false)).add('=').add(Token.uri(value.string(ii), false));
      }
    }
    return Str.get(tb.finish());
  }
}
