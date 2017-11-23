package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class UtilItemAt extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final double dp = toDouble(exprs[1], qc);
    final long ps = (long) dp;
    if(dp != ps || ps < 1) return null;

    // if possible, retrieve single item
    final Expr ex = exprs[0];
    if(ex.seqType().zeroOrOne()) return ps == 1 ? ex.item(qc, info) : null;

    // fast route if the size is known
    final Iter iter = qc.iter(ex);
    final long is = iter.size();
    if(is >= 0) return ps > is ? null : iter.get(ps - 1);

    // loop through all items
    long p = 0;
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      if(++p == ps) return it;
    }
    return null;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex = exprs[0], pos = exprs[1];
    final SeqType st = ex.seqType();
    if(st.zero()) return ex;
    exprType.assign(st.type);

    if(pos instanceof Value) {
      final double dp = toDouble(pos, cc.qc);
      final long ps = (long) dp;
      // invalid positions
      if(dp != ps || ps < 1) return Empty.SEQ;
      // pre-evaluate single expression with static position
      if(st.zeroOrOne()) return ps == 1 ? ex : Empty.SEQ;
      // check for large values
      if(ex instanceof Value) return ps <= ex.size() ? ((Value) ex).itemAt(ps - 1) : Empty.SEQ;
    }
    return this;
  }
}
