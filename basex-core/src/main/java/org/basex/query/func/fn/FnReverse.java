package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnReverse extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // optimization: reverse sequence
    if(exprs[0] instanceof Value) return value(qc).iter();

    // materialize value if number of results is unknown
    final Iter iter = qc.iter(exprs[0]);
    final long s = iter.size();
    if(s == -1) {
      // estimate result size (could be known in the original expression)
      final ValueBuilder vb = new ValueBuilder(Math.max((int) exprs[0].size(), 1));
      for(Item it; (it = iter.next()) != null;) vb.add(it);
      Array.reverse(vb.items(), 0, (int) vb.size());
      return vb;
    }

    // return iterator if items can be directly accessed
    return s == 0 ? Empty.ITER : s == 1 ? iter : new Iter() {
      long c = s;
      @Override
      public Item next() throws QueryException { return --c >= 0 ? iter.get(c) : null; }
      @Override
      public Item get(final long i) throws QueryException { return iter.get(s - i - 1); }
      @Override
      public long size() { return s; }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value v = exprs[0].value(qc);
    return v.size() > 1 ? ((Seq) v).reverse() : v;
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    seqType = exprs[0].seqType();
    return this;
  }
}
