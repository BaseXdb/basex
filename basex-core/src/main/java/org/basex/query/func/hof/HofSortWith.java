package org.basex.query.func.hof;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class HofSortWith extends HofFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value v = exprs[0].value(qc);
    final Comparator<Item> cmp = getComp(1, qc);
    if(v.size() < 2) return v;

    final long sz = v.size();
    if(sz > Integer.MAX_VALUE) throw RANGE_X.get(info, sz);

    final ItemList items = new ItemList((int) sz);
    final Iter iter = v.iter();
    for(Item it; (it = qc.next(iter)) != null;) items.add(it);

    try {
      Arrays.sort(items.list, 0, items.size(), cmp);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return items.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr ex = exprs[0];
    return ex.seqType().zero() ? ex : adoptType(ex);
  }
}
