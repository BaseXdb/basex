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
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public class FnTakeWhile extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 2, qc);
    return new Iter() {
      int p;

      @Override
      public Item next() throws QueryException {
        final Item item = qc.next(input);
        return item != null && toBoolean(qc, predicate, item, Int.get(++p)) ? item : null;
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
