package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnInnermost extends FnOutermost {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return most(qc, false);
  }
}
