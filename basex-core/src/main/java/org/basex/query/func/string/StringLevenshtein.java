package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class StringLevenshtein extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value1 = toToken(exprs[0], qc), value2 = toToken(exprs[1], qc);

    final int[] cps1 = new TokenParser(value1).toArray(), cps2 = new TokenParser(value2).toArray();
    return Dbl.get(Levenshtein.distance(cps1, cps2));
  }
}
