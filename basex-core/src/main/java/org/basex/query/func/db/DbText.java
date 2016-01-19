package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
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
    final MetaData meta = data.meta;
    if(!data.meta.index(type)) throw BXDB_INDEX_X.get(info, meta.name, type);
    return new ValueAccess(info, exprs[1], type, null, new IndexContext(data, false));
  }
}
