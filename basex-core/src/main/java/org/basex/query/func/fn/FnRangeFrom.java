package org.basex.query.func.fn;

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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class FnRangeFrom extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = exprs[0].iter(qc);
      final FItem start = toFunction(exprs[1], 1, qc);
      boolean started;

      @Override
      public Item next() throws QueryException {
        for(Item item; (item = input.next()) != null;) {
          if(started || toBoolean(start.invoke(qc, info, item).item(qc, info))) {
            started = true;
            return item;
          }
        }
        return null;
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    exprs[1] = coerceFunc(exprs[1], cc, SeqType.ITEM_ZM, st.with(Occ.EXACTLY_ONE));
    exprType.assign(st.union(Occ.ZERO));
    data(input.data());
    return this;
  }
}
