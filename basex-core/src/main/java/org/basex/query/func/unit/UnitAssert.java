package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UnitAssert extends UnitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(arg(0).ebv(qc, info)) return Empty.VALUE;
    throw error(toNodeOrAtomItem(arg(1), true, qc));
  }
}
