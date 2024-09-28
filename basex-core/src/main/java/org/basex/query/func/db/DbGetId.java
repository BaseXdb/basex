package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class DbGetId extends DbAccess {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final Iter values = arg(1).atomIter(qc, info);

    final IntList list = new IntList(Seq.initialCapacity(values.size()));
    for(Item item; (item = qc.next(values)) != null;) {
      final int id = (int) toLong(item), pre = pre(id, data);
      if(pre < 0 || pre >= data.meta.size) throw DB_RANGE_X_X.get(info, data.meta.name, id);
      list.add(pre);
    }
    return DBNodeSeq.get(list.ddo().finish(), data, this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    compileData(cc);

    final Expr nodes = arg(0);
    final Data data = nodes.data();
    if(data != null && !data.meta.updindex && !(this instanceof DbGetPre)) {
      // no ID-PRE mapping: work with PRE values
      return cc.function(_DB_GET_PRE, info, nodes);
    }
    return this;
  }

  @Override
  public final boolean ddo() {
    return true;
  }

  /**
   * Returns the pre value for the specified id.
   * @param id id
   * @param data data reference
   * @return pre value
   */
  protected int pre(final int id, final Data data) {
    return data.pre(id);
  }
}
