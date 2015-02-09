package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class DbOpenPre extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return open(qc, false);
  }

  /**
   * Performs the open-id and open-pre function.
   * @param qc query context
   * @param id id flag
   * @return result
   * @throws QueryException query exception
   */
  final DBNode open(final QueryContext qc, final boolean id) throws QueryException {
    final Data data = checkData(qc);
    final int v = (int) toLong(exprs[1], qc);
    final int pre = id ? data.pre(v) : v;
    if(pre >= 0 && pre < data.meta.size) return new DBNode(data, pre);
    throw BXDB_RANGE_X_X_X.get(info, data.meta.name, id ? "ID" : "pre", v);
  }
}
