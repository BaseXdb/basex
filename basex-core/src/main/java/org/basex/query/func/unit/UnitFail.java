package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UnitFail extends UnitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    throw error(exprs.length > 0 ? toNodeOrAtomItem(0, qc) : null);
  }
}
