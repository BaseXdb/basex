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
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnFoldLeft extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[2], 2, qc);

    Value value = exprs[1].value(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      value = func.invoke(qc, info, value, item);
    }
    return value;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[2], 2, qc);

    // don't convert to a value if not necessary
    Item item = iter.next();
    if(item == null) return exprs[1].iter(qc);

    Value value = exprs[1].value(qc);
    do {
      value = func.invoke(qc, info, value, item);
    } while((item = qc.next(iter)) != null);
    return value.iter();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1 == Empty.VALUE) return expr2;

    opt(this, cc, false, true);

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
   * Refines the types of a fold function.
   * @param sf function
   * @param cc compilation context
   * @param array indicates if this is an array function
   * @param left indicates if this is a left/right fold
   * @throws QueryException query exception
   */
  public static void opt(final StandardFunc sf, final CompileContext cc, final boolean array,
      final boolean left) throws QueryException {

    final Expr[] exprs = sf.exprs;
    final Expr func = exprs[2];
    if(func instanceof FuncItem) {
      // function argument is a single function item
      final SeqType seq = exprs[0].seqType(), zero = exprs[1].seqType(), curr = array &&
          seq.type instanceof ArrayType ? ((ArrayType) seq.type).declType : seq.with(Occ.ONE);

      // assign item type of iterated value, optimize function
      final SeqType[] args = { left ? SeqType.ITEM_ZM : curr, left ? curr : SeqType.ITEM_ZM };
      Expr optFunc = sf.coerceFunc(func, cc, SeqType.ITEM_ZM, args);

      final FuncType ft = optFunc.funcType();
      final int i = left ? 0 : 1;
      SeqType input = zero, output = ft.declType;

      // if initial item has more specific type, assign it and check optimized result type
      final SeqType at = ft.argTypes[i];
      if(!input.eq(at) && input.instanceOf(at)) {
        do {
          args[i] = input;
          optFunc = sf.coerceFunc(func, cc, ft.declType, args);
          output = optFunc.funcType().declType;

          // optimized type is instance of input type: abort
          if(output.instanceOf(input)) break;
          // combine input and output type, optimize again
          input = input.union(output);
        } while(true);
      }

      sf.exprType.assign(array || !seq.oneOrMore() ? output.union(zero) : output);
      exprs[2] = optFunc;
    }
  }
}
