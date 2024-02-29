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
    final FItem action = action(qc);
    Value result = arg(1).value(qc);

    final Iter input = arg(0).iter(qc);
    long p = input.size();
    if(p != -1) {
      for(; p > 0; p--) {
        final Item item = input.get(p - 1);
        result = action.invoke(qc, info, item, result, Int.get(p));
        if(skip(qc, item, result)) break;
      }
    } else {
      final Value value = input.value(qc, arg(0));
      p = value.size();
      if(value instanceof TreeSeq) {
        final TreeSeq seq = (TreeSeq) value;
        for(final ListIterator<Item> iter = seq.iterator(p); iter.hasPrevious();) {
          final Item item = iter.previous();
          result = action.invoke(qc, info, item, result, Int.get(p--));
          if(skip(qc, item, result)) break;
        }
      } else {
        for(; p > 0; p--) {
          final Item item = value.itemAt(p - 1);
          result = action.invoke(qc, info, item, result, Int.get(p));
          if(skip(qc, item, result)) break;
        }
      }
    }
    return result;
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
        final Expr func = coerce(2, cc, arity);
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
