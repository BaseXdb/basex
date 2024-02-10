package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class UnitFail extends UnitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    throw error(defined(0) ? toNodeOrAtomItem(arg(0), qc) : null);
  }
}
