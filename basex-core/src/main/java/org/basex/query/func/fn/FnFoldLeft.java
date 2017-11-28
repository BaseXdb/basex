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
    final Expr ex1 = exprs[0], ex2 = exprs[1];
    if(ex1 == Empty.SEQ) return ex2;

    seqType(this, cc, false, true);

    if(allAreValues(false) && ex1.size() <= UNROLL_LIMIT) {
      // unroll the loop
      Expr ex = ex2;
      for(final Item it : (Value) ex1) {
        ex = new DynFuncCall(info, sc, exprs[2], ex, it).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return ex;
    }
    return this;
  }

  /**
   * Assigns more specific sequence types.
   * @param func function
   * @param cc compilation context
   * @param array indicates is this is array function
   * @param left indicates is this is left/right fold
   * @throws QueryException query exception
   */
  public static void seqType(final StandardFunc func, final CompileContext cc,
      final boolean array, final boolean left) throws QueryException {

    final Expr[] exprs = func.exprs;
    final SeqType st1 = exprs[0].seqType(), st2 = exprs[1].seqType();
    if(exprs[2].seqType().type instanceof FuncType) {
      final SeqType ft = array && st1.type instanceof ArrayType ?
        ((ArrayType) st1.type).declType : st1;
      func.coerceFunc(2, cc, SeqType.ITEM_ZM,
          left ? SeqType.ITEM_ZM : ft, left ? ft : SeqType.ITEM_ZM);

      final SeqType dt = ((FuncType) exprs[2].seqType().type).declType;
      func.exprType.assign(array || st1.mayBeEmpty() ? dt.union(st2) : dt);
    }
  }
}
