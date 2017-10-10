package org.basex.query.func.hof;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

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
    final ItemList items = v.cache();
    try {
      Arrays.sort(items.list, 0, items.size(), cmp);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return items.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final SeqType st = exprs[0].seqType();
    if(st.zero()) return exprs[0];
    seqType = st;
    return this;
  }
}
