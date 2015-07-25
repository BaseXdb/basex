package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnId extends Ids {
  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    return ids(qc, false);
  }
}
