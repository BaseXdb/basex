package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.tree.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnFoldRight extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value v = qc.value(exprs[0]);
    Value res = qc.value(exprs[1]);
    final FItem fun = checkArity(exprs[2], 2, qc);
    if(v instanceof TreeSeq) {
      final ListIterator<Item> iter = ((TreeSeq) v).iterator(v.size());
      while(iter.hasPrevious()) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, iter.previous(), res);
      }
    } else {
      for(long i = v.size(); --i >= 0;) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, v.itemAt(i), res);
      }
    }
    return res;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value v = qc.value(exprs[0]);
    final FItem fun = checkArity(exprs[2], 2, qc);

    // evaluate start value lazily if it's passed straight through
    if(v.isEmpty()) return qc.iter(exprs[1]);

    Value res = qc.value(exprs[1]);
    if(v instanceof TreeSeq) {
      final ListIterator<Item> iter = ((TreeSeq) v).iterator(v.size());
      while(iter.hasPrevious()) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, iter.previous(), res);
      }
    } else {
      for(long i = v.size(); --i >= 0;) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, v.itemAt(i), res);
      }
    }
    return res.iter();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(allAreValues() && exprs[0].size() < FnForEach.UNROLL_LIMIT) {
      // unroll the loop
      final Value seq = (Value) exprs[0];
      Expr e = exprs[1];
      for(int i = (int) seq.size(); --i >= 0;) {
        e = new DynFuncCall(info, sc, exprs[2], seq.itemAt(i), e).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return e;
    }
    return this;
  }
}
