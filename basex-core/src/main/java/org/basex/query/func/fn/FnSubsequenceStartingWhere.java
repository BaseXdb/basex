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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnSubsequenceStartingWhere extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = arg(0).iter(qc);
      final FItem predicate = toFunction(arg(1), 2, qc);
      boolean started;
      int p;

      @Override
      public Item next() throws QueryException {
        for(Item item; (item = input.next()) != null;) {
          if(started) return item;
          if(toBoolean(qc, predicate, item, Int.get(++p))) {
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
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    arg(1, arg -> refineFunc(arg, cc, SeqType.ITEM_ZM, st.with(Occ.EXACTLY_ONE),
        SeqType.INTEGER_O));
    exprType.assign(st.union(Occ.ZERO)).data(input);
    return this;
  }
}
