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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnFoldRight extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value seq = exprs[0].value(qc);
    Value res = exprs[1].value(qc);
    final FItem fun = checkArity(exprs[2], 2, qc);
    if(seq instanceof TreeSeq) {
      final ListIterator<Item> iter = ((TreeSeq) seq).iterator(seq.size());
      while(iter.hasPrevious()) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, iter.previous(), res);
      }
    } else {
      for(long i = seq.size(); --i >= 0;) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, seq.itemAt(i), res);
      }
    }
    return res;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value seq = exprs[0].value(qc);
    final FItem fun = checkArity(exprs[2], 2, qc);

    // evaluate start value lazily if it's passed straight through
    if(seq.isEmpty()) return exprs[1].iter(qc);

    Value res = exprs[1].value(qc);
    if(seq instanceof TreeSeq) {
      final ListIterator<Item> iter = ((TreeSeq) seq).iterator(seq.size());
      while(iter.hasPrevious()) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, iter.previous(), res);
      }
    } else {
      for(long i = seq.size(); --i >= 0;) {
        qc.checkStop();
        res = fun.invokeValue(qc, info, seq.itemAt(i), res);
      }
    }
    return res.iter();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex1 = exprs[0], ex2 = exprs[1];
    if(ex1 == Empty.SEQ) return ex2;

    FnFoldLeft.seqType(this, cc, false, false);

    if(allAreValues(false) && ex1.size() <= UNROLL_LIMIT) {
      // unroll the loop
      Expr ex = ex2;
      for(final Item it : ((Value) ex1).reverse(cc.qc)) {
        ex = new DynFuncCall(info, sc, exprs[2], it, ex).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return ex;
    }
    return this;
  }
}
