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
    final Value v = qc.value(exprs[0]);
    final Comparator<Item> cmp = getComp(1, qc);
    if(v.size() < 2) return v;

    final long n = v.size();
    if(n > Integer.MAX_VALUE) throw RANGE_X.get(info, n);

    final ItemList items = new ItemList((int) n);
    for(final Item it : v) {
      qc.checkStop();
      items.add(it);
    }

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
