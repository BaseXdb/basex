package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
    final Value input = arg(0).value(qc);
    final FItem action = action(qc);

    Value result = arg(1).value(qc);
    if(input instanceof TreeSeq) {
      final TreeSeq seq = (TreeSeq) input;
      for(final ListIterator<Item> iter = seq.iterator(input.size()); iter.hasPrevious();) {
        final Item item = iter.previous();
        result = action.invoke(qc, info, item, result);
        if(skip(qc, item, result)) break;
      }
    } else {
      for(long i = input.size(); --i >= 0;) {
        final Item item = input.itemAt(i);
        result = action.invoke(qc, info, item, result);
        if(skip(qc, item, result)) break;
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
        expr = zero;
        for(int es = unroll.size() - 1; es >= 0; es--) {
          expr = new DynFuncCall(info, sc, action, unroll.get(es), expr).optimize(cc);
        }
        return expr;
      }
    }
    return this;
  }
}
