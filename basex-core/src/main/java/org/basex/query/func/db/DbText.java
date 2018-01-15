package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class DbText extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return valueAccess(IndexType.TEXT, qc).iter(qc);
  }

  /**
   * Returns an index accessor.
   * @param type index type
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  final ValueAccess valueAccess(final IndexType type, final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    return new ValueAccess(info, exprs[1], type, null, new IndexStaticDb(info, data));
  }
}
