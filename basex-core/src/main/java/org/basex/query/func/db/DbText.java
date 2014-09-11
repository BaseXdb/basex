package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DbText extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return valueAccess(true, qc).iter(qc);
  }

  /**
   * Returns an index accessor.
   * @param text text/attribute flag
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  final ValueAccess valueAccess(final boolean text, final QueryContext qc)
      throws QueryException {
    return new ValueAccess(info, exprs[1], text, null, new IndexContext(checkData(qc), false));
  }
}
