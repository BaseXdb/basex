package org.basex.query.func.hof;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class HofSortWith extends HofFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    final Comparator<Item> comp = getComp(1, qc);
    if(value.size() < 2) return value;

    final ItemList items = new ItemList(Seq.initialCapacity(value.size()));
    final Iter iter = value.iter();
    for(Item item; (item = qc.next(iter)) != null;) items.add(item);

    try {
      Arrays.sort(items.list, 0, items.size(), comp);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return items.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr = exprs[0];
    return expr.seqType().zero() ? expr : adoptType(expr);
  }
}
