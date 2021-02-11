package org.basex.query.func.util;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilItem extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // retrieve (possibly invalid) position
    final long pos = pos(qc);
    if(pos < 0) return Empty.VALUE;

    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(expr.seqType().zeroOrOne()) return pos == 0 ? expr.item(qc, info) : Empty.VALUE;

    // fast route if the size is known
    final Iter iter = expr.iter(qc);
    final long size = iter.size();
    if(size >= 0) return pos < size ? iter.get(pos) : Empty.VALUE;

    // iterate until specified item is found
    long p = pos;
    for(Item item; (item = qc.next(iter)) != null;) {
      if(p-- == 0) return item;
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0], position = exprs[1];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    if(position instanceof Value) {
      // retrieve (possibly invalid) position
      final long pos = pos(cc.qc);
      if(pos < 0) return Empty.VALUE;

      // pre-evaluate single expression with static position
      if(st.zeroOrOne()) return pos == 0 ? expr : Empty.VALUE;
      // pre-evaluate values
      final long size = expr.size();
      if(size != -1) {
        if(pos + 1 == size) return cc.function(_UTIL_LAST, info, expr);
        if(pos + 1 > size) return Empty.VALUE;
        if(REVERSE.is(expr))
          return cc.function(_UTIL_ITEM, info, expr.arg(0), Int.get(size - pos));
      }
      if(pos == 0) return cc.function(HEAD, info, expr);

      // rewrite nested function calls
      if(TAIL.is(expr))
        return cc.function(_UTIL_ITEM, info, expr.arg(0), Int.get(pos + 2));
      if(_UTIL_REPLICATE.is(expr)) {
        // static integer will always be greater than 1
        final Expr[] args = expr.args();
        if(args[0].size() == 1 && args[1] instanceof Int) {
          final long count = ((Int) args[1]).itr();
          return pos > count ? Empty.VALUE : args[0];
        }
      }
      if(_FILE_READ_TEXT_LINES.is(expr))
        return FileReadTextLines.opt(this, pos, 1, cc);

      // rewrite to head function
      if(expr instanceof List) {
        final Expr[] args = expr.args();
        final int al = args.length;
        for(int a = 0; a < al; a++) {
          if(a == pos) {
            final Expr list = List.get(cc, info, Arrays.copyOfRange(args, a, al));
            return cc.function(HEAD, info, list);
          }
          if(!args[a].seqType().one()) break;
        }
      }
    }

    if(_UTIL_INIT.is(expr))
      return cc.function(_UTIL_ITEM, info, expr.arg(0), position);

    exprType.assign(st.with(Occ.ZERO_OR_ONE));
    data(expr.data());
    return this;
  }

  /**
   * Returns the item position (starting with 0).
   * @param qc query context
   * @return position or {@code -1}
   * @throws QueryException query exception
   */
  private long pos(final QueryContext qc) throws QueryException {
    final double dp = toDouble(exprs[1], qc);
    final long pos = (long) dp;
    return dp != pos ? -1 : pos - 1;
  }
}
