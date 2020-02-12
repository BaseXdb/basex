package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class DbOpenPre extends DbAccess {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return open(qc, false);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    return compileData(cc);
  }

  /**
   * Performs the open-id and open-pre function.
   * @param qc query context
   * @param id id flag
   * @return result
   * @throws QueryException query exception
   */
  final Value open(final QueryContext qc, final boolean id) throws QueryException {
    final Data data = checkData(qc);
    final Iter iter = exprs[1].atomIter(qc, info);

    // handle arbitrary input
    final IntList il = new IntList(Math.max(8, (int) iter.size()));
    for(Item item; (item = qc.next(iter)) != null;) {
      final int v = (int) toLong(item);
      final int pre = id ? data.pre(v) : v;
      if(pre < 0 || pre >= data.meta.size)
        throw DB_RANGE_X_X_X.get(info, data.meta.name, id ? "ID" : "pre", v);
      il.add(pre);
    }
    return DBNodeSeq.get(il.ddo().finish(), data, this);
  }
}
