package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnIdref extends Ids {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final NodeSeqBuilder nb = new NodeSeqBuilder().check();
    add(ids(exprs[0].atomIter(qc, info)), nb, checkRoot(toNode(arg(1, qc), qc)), true);
    return nb;
  }
}
