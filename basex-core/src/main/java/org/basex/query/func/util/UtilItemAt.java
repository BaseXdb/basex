package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class UtilItemAt extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final double ps = toDouble(exprs[1], qc);
    final long pos = (long) ps;
    if(ps != pos || pos < 1) return null;

    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(expr.seqType().zeroOrOne()) return pos == 1 ? expr.item(qc, info) : null;

    // fast route if the size is known
    final Iter iter = expr.iter(qc);
    final long size = iter.size();
    if(size >= 0) return pos > size ? null : iter.get(pos - 1);

    // iterate until specified item is found
    long p = 0;
    for(Item item; (item = qc.next(iter)) != null;) {
      if(++p == pos) return item;
    }
    return null;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0], pos = exprs[1];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;
    exprType.assign(st.type);

    if(pos instanceof Value) {
      final double dp = toDouble(pos, cc.qc);
      final long ps = (long) dp;
      // invalid positions
      if(dp != ps || ps < 1) return Empty.SEQ;
      // pre-evaluate single expression with static position
      if(st.zeroOrOne()) return ps == 1 ? expr : Empty.SEQ;
      // pre-evaluate values
      if(expr instanceof Value) {
        return ps <= expr.size() ? ((Value) expr).itemAt(ps - 1) : Empty.SEQ;
      }
      // rewrite retrieval of first item
      if(ps == 1) return cc.function(Function.HEAD, info, exprs[0]);

      // faster retrieval of single line
      return FileReadTextLines.rewrite(this, ps, 1, cc, info);
    }
    return this;
  }
}
