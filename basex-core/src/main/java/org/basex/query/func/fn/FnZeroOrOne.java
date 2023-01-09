package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnZeroOrOne extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final Item item = input.next();
    if(item == null) return Empty.VALUE;
    if(input.next() == null) return item;
    throw ZEROORONE.get(info);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zeroOrOne()) return input;
    if(input.size() > 1) throw ZEROORONE.get(info);

    exprType.assign(st.with(Occ.ZERO_OR_ONE)).data(input);
    return this;
  }
}
