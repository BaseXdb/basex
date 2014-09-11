package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class MapForEachEntry extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return toMap(exprs[0], qc).apply(checkArity(exprs[1], 2, qc), qc, info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value();
  }
}
