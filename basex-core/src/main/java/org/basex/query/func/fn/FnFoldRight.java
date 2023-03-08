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
public final class FnFoldRight extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    return value(input, qc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    return input.isEmpty() ? arg(1).iter(qc) : value(input, qc).iter();
  }

  /**
   * Evaluates the expression.
   * @param items items to process
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Value value(final Value items, final QueryContext qc) throws QueryException {
    Value value = arg(1).value(qc);
    final FItem action = toFunction(arg(2), 2, qc);

    if(items instanceof TreeSeq) {
      for(final ListIterator<Item> iter = ((TreeSeq) items).iterator(items.size());
          iter.hasPrevious();) {
        value = action.invoke(qc, info, iter.previous(), value);
      }
    } else {
      for(long i = items.size(); --i >= 0;) {
        value = action.invoke(qc, info, items.itemAt(i), value);
      }
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), zero = arg(1), action = arg(2);
    if(input.seqType().zero()) return zero;

    FnFoldLeft.opt(this, cc, false, false);

    // unroll fold
    if(action instanceof Value) {
      final ExprList unroll = cc.unroll(input, true);
      if(unroll != null) {
        Expr expr = zero;
        for(int es = unroll.size() - 1; es >= 0; es--) {
          expr = new DynFuncCall(info, sc, action, unroll.get(es), expr).optimize(cc);
        }
        return expr;
      }
    }
    return this;
  }
}
