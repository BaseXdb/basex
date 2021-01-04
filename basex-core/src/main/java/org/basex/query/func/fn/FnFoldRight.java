package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.seq.tree.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnFoldRight extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value seq = exprs[0].value(qc);
    return value(seq, qc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value seq = exprs[0].value(qc);
    return seq.isEmpty() ? exprs[1].iter(qc) : value(seq, qc).iter();
  }

  /**
   * Evaluates the expression.
   * @param items items to process
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Value value(final Value items, final QueryContext qc) throws QueryException {
    Value value = exprs[1].value(qc);
    final FItem func = checkArity(exprs[2], 2, qc);

    if(items instanceof TreeSeq) {
      final ListIterator<Item> iter = ((TreeSeq) items).iterator(items.size());
      while(iter.hasPrevious()) {
        value = func.invoke(qc, info, iter.previous(), value);
      }
    } else {
      for(long i = items.size(); --i >= 0;) {
        value = func.invoke(qc, info, items.itemAt(i), value);
      }
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1 == Empty.VALUE) return expr2;

    FnFoldLeft.opt(this, cc, false, false);

    if(allAreValues(false) && expr1.size() <= UNROLL_LIMIT) {
      // unroll the loop
      Expr expr = expr2;
      for(final Item item : ((Value) expr1).reverse(cc.qc)) {
        expr = new DynFuncCall(info, sc, exprs[2], item, expr).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return expr;
    }
    return this;
  }
}
