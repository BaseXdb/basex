package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class UnitFail extends UnitFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    throw error(toNodeOrAtomItem(0, qc));
  }
}
