package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.tree.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnFoldRight extends FnFoldLeft {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem action = action(qc);
    Value result = arg(1).value(qc);

    final Iter input = arg(0).iter(qc);
    final long size = input.size();
    if(size != -1) {
      for(long s = size - 1; s >= 0; s--) {
        final Item item = input.get(s);
        result = action.invoke(qc, info, item, result);
        if(skip(qc, item, result)) break;
      }
    } else {
      final Value value = input.value(qc, arg(0));
      if(value instanceof TreeSeq) {
        final TreeSeq seq = (TreeSeq) value;
        for(final ListIterator<Item> iter = seq.iterator(input.size()); iter.hasPrevious();) {
          final Item item = iter.previous();
          result = action.invoke(qc, info, item, result);
          if(skip(qc, item, result)) break;
        }
      } else {
        for(long i = input.size(); --i >= 0;) {
          final Item item = value.itemAt(i);
          result = action.invoke(qc, info, item, result);
          if(skip(qc, item, result)) break;
        }
      }
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = opt(cc, false, false);
    if(expr != this) return expr;

    // unroll fold
    final Expr input = arg(0), zero = arg(1), action = arg(2);
    if(action instanceof Value) {
      final ExprList unroll = cc.unroll(input, true);
      if(unroll != null) {
        final Expr func = coerce(2, cc);
        expr = zero;
        for(int es = unroll.size() - 1; es >= 0; es--) {
          expr = new DynFuncCall(info, sc, func, unroll.get(es), expr).optimize(cc);
        }
        return expr;
      }
    }
    return this;
  }
}
