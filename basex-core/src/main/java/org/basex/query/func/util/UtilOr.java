package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class UtilOr extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final long size = iter.size();

    // size is known, results exist: return items iterator
    if(size > 0) return iter;
    // no results: return default iterator
    if(size == 0) return exprs[1].iter(qc);

    // iterator yields no result: return default iterator
    final Item item = iter.next();
    if(item == null) return exprs[1].iter(qc);

    return new Iter() {
      boolean more;
      @Override
      public Item next() throws QueryException {
        if(more) return qc.next(iter);
        more = true;
        return item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    return value.isEmpty() ? exprs[1].value(qc) : value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr items = exprs[0], def = exprs[1];
    if(items == Empty.SEQ) return def;
    final SeqType st = items.seqType();
    if(st.oneOrMore()) return items;
    exprType.assign(st.union(def.seqType()));
    return this;
  }
}
