package org.basex.query.func.fn;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnZeroOrOne extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Iter ir = exprs[0].iter(qc);
    Item it = ir.next();
    if(it != null && ir.next() != null) throw ZEROORONE.get(info);
    return it;
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    final Expr e = exprs[0];
    final SeqType st = e.seqType();
    seqType = SeqType.get(st.type, Occ.ZERO_ONE);
    return st.zeroOrOne() ? e : this;
  }
}
