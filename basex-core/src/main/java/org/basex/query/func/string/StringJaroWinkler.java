package org.basex.query.func.string;

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
public final class StringJaroWinkler extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);

    final int[] cps1 = Token.cps(value1), cps2 = Token.cps(value2);
    return Dbl.get(JaroWinkler.distance(cps1, cps2));
  }
}
