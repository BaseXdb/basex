package org.basex.query.func.strings;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringsSoundex extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int[] cps = new TokenParser(toToken(exprs[0], qc)).toArray();
    final int[] encoded = Soundex.encode(cps);

    final TokenBuilder tb = new TokenBuilder(encoded.length);
    for(final int cp : encoded) tb.add(cp);
    return Str.get(tb.finish());
  }
}
