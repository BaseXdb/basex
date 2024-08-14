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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnFoldRight extends FnFoldLeft {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = action(qc);

    final HofArgs args = new HofArgs(3, action).set(1, arg(1).value(qc));
    long p = input.size();
    if(p != -1) {
      for(; p > 0; p--) {
        args.set(1, invoke(action, args.set(0, input.get(p - 1)).pos(p), qc));
        if(skip(qc, args)) break;
      }
    } else {
      final Value value = input.value(qc, arg(0));
      p = value.size();
      if(value instanceof TreeSeq) {
        final TreeSeq seq = (TreeSeq) value;
        for(final ListIterator<Item> iter = seq.iterator(p); iter.hasPrevious();) {
          args.set(1, invoke(action, args.set(0, iter.previous()).pos(p--), qc));
          if(skip(qc, args)) break;
        }
      } else {
        for(; p > 0; p--) {
          args.set(1, invoke(action, args.set(0, value.itemAt(p - 1)).pos(p), qc));
          if(skip(qc, args)) break;
        }
      }
    }
    return args.get(1);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = optType(cc, false, false);
    if(expr != this) return expr;

    // unroll fold
    final Expr input = arg(0), zero = arg(1), action = arg(2);
    final int arity = arity(action);
    if(action instanceof Value && arity == 2) {
      final ExprList unroll = cc.unroll(input, true);
      if(unroll != null) {
        final Expr func = coerceFunc(2, cc, arity);
        expr = zero;
        for(int es = unroll.size() - 1; es >= 0; es--) {
          expr = new DynFuncCall(info, func, unroll.get(es), expr).optimize(cc);
        }
        return expr;
      }
    }
    return this;
  }
}
