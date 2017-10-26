package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnFoldLeft extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final FItem fun = checkArity(exprs[2], 2, qc);
    Value res = qc.value(exprs[1]);
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      res = fun.invokeValue(qc, info, res, it);
    }
    return res;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final FItem fun = checkArity(exprs[2], 2, qc);

    // don't convert to a value if not necessary
    Item it = iter.next();
    if(it == null) return qc.iter(exprs[1]);

    Value res = qc.value(exprs[1]);
    do {
      qc.checkStop();
      res = fun.invokeValue(qc, info, res, it);
    } while((it = iter.next()) != null);
    return res.iter();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(exprs[0] == Empty.SEQ) return exprs[1];
    if(allAreValues() && exprs[0].size() < FnForEach.UNROLL_LIMIT) {
      // unroll the loop
      final Value seq = (Value) exprs[0];
      Expr e = exprs[1];
      for(final Item it : seq) {
        e = new DynFuncCall(info, sc, exprs[2], e, it).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return e;
    }
    refineType(this);
    return this;
  }

  /**
   * Refines the function type.
   * @param func function
   */
  public static void refineType(final StandardFunc func) {
    final Expr[] exprs = func.exprs;
    final Type t = exprs[2].seqType().type;
    if(t instanceof FuncType) {
      final SeqType vt = ((FuncType) t).valueType;
      if(exprs[0].seqType().mayBeEmpty()) {
        func.seqType = vt.union(exprs[1].seqType());
      } else {
        func.seqType = vt;
      }
    }
  }
}
