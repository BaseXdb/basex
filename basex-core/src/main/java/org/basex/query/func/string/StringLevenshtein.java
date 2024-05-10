package org.basex.query.func.string;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class StringLevenshtein extends StandardFunc {
  /** Maximum size of supported string length. */
  private static final int MAX_LENGTH = 10000;

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);

    final int[] cps1 = new TokenParser(value1).toArray(), cps2 = new TokenParser(value2).toArray();
    final int cl1 = cps1.length, cl2 = cps2.length;
    if(cl1 > MAX_LENGTH || cl2 > MAX_LENGTH) throw STRING_BOUNDS_X.get(info, MAX_LENGTH);
    return Dbl.get(Levenshtein.distance(cps1, cps2));
  }
}
