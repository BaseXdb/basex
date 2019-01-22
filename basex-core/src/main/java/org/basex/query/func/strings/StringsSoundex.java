package org.basex.query.func.strings;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class StringsSoundex extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int[] cps = Soundex.encode(new TokenParser(toToken(exprs[0], qc)).toArray());

    final TokenBuilder tb = new TokenBuilder(cps.length);
    for(final int cp : cps) tb.add(cp);
    return Str.get(tb.finish());
  }
}
