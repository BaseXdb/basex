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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnFoldLeft extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[2], 2, qc);

    Value res = exprs[1].value(qc);
    for(Item item; (item = qc.next(iter)) != null;) res = func.invokeValue(qc, info, res, item);
    return res;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[2], 2, qc);

    // don't convert to a value if not necessary
    Item item = iter.next();
    if(item == null) return exprs[1].iter(qc);

    Value res = exprs[1].value(qc);
    do res = func.invokeValue(qc, info, res, item); while((item = qc.next(iter)) != null);
    return res.iter();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1 == Empty.SEQ) return expr2;

    seqType(this, cc, false, true);

    if(allAreValues(false) && expr1.size() <= UNROLL_LIMIT) {
      // unroll the loop
      Expr expr = expr2;
      for(final Item item : (Value) expr1) {
        expr = new DynFuncCall(info, sc, exprs[2], expr, item).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return expr;
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
  public static void seqType(final StandardFunc func, final CompileContext cc, final boolean array,
      final boolean left) throws QueryException {

    final Expr[] exprs = func.exprs;
    final SeqType st1 = exprs[0].seqType(), st2 = exprs[1].seqType();
    if(exprs[2].seqType().type instanceof FuncType) {
      final SeqType fst1 = array && st1.type instanceof ArrayType ?
        ((ArrayType) st1.type).declType : st1;
      func.coerceFunc(2, cc, SeqType.ITEM_ZM,
          left ? SeqType.ITEM_ZM : fst1, left ? fst1 : SeqType.ITEM_ZM);

      final SeqType dt = ((FuncType) exprs[2].seqType().type).declType;
      func.exprType.assign(array || st1.mayBeEmpty() ? dt.union(st2) : dt);
    }
  }
}
