package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnOneOrMore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final long size = iter.size();
    if(size == 0) throw ONEORMORE.get(info);
    if(size > 0) return iter;

    return new Iter() {
      private boolean first = true;
      @Override
      public Item next() throws QueryException {
        final Item item = qc.next(iter);
        if(first) {
          if(item == null) throw ONEORMORE.get(info);
          first = false;
        }
        return item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    if(value.isEmpty()) throw ONEORMORE.get(info);
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.oneOrMore()) return expr;
    if(st.zero()) throw ONEORMORE.get(info);

    exprType.assign(st.with(Occ.ONE_OR_MORE));
    data(expr.data());
    return this;
  }
}
