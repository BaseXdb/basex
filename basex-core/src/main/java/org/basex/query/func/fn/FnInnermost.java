package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnInnermost extends Nodes {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return most(qc, false);
  }
}
