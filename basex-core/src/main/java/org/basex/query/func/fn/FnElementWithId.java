package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnElementWithId extends Ids {
  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final ANodeList list = new ANodeList().check();
    add(ids(exprs[0].atomIter(qc, info)), list, checkRoot(toNode(arg(1, qc), qc)), false);
    return list.iter();
  }
}
