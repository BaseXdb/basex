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
public final class StringsLevenshtein extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int[] cps1 = new TokenParser(toToken(exprs[0], qc)).toArray();
    final int[] cps2 = new TokenParser(toToken(exprs[1], qc)).toArray();
    return Dbl.get(Levenshtein.distance(cps1, cps2));
  }
}
