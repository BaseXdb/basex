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
public final class FnExactlyOne extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr input = exprs[0];
    Item item;
    if(input.seqType().zeroOrOne()) {
      item = input.item(qc, info);
      if(item != Empty.VALUE) return item;
    } else {
      final Iter iter = input.iter(qc);
      item = iter.next();
      if(item != null && iter.next() != null) item = null;
      if(item != null) return item;
    }
    throw EXACTLYONE.get(info);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.one()) return input;
    if(st.zero() || input.size() > 1) throw EXACTLYONE.get(info);

    exprType.assign(st.with(Occ.EXACTLY_ONE)).data(input);
    return this;
  }
}
