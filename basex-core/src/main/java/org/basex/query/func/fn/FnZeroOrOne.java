package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnZeroOrOne extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final Item it = iter.next();
    if(it != null && iter.next() != null) throw ZEROORONE.get(info);
    return it;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr e = exprs[0];
    final SeqType st = e.seqType();
    if(st.zeroOrOne()) return e;
    if(st.occ.min > 1) throw ZEROORONE.get(info);
    seqType = st;
    return this;
  }
}
