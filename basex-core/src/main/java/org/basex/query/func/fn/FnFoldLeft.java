package org.basex.query.func.fn;

import org.basex.core.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnFoldLeft extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final Item item = iter.next();
    return item != null ? value(iter, item, qc) : exprs[1].value(qc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final Item item = iter.next();
    return item != null ? value(iter, item, qc).iter() : exprs[1].iter(qc);
  }

  /**
   * Evaluates the expression.
   * @param iter input iterator
   * @param item first item
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Value value(final Iter iter, final Item item, final QueryContext qc)
      throws QueryException {
    Value value = exprs[1].value(qc);
    final FItem func = checkArity(exprs[2], 2, qc);
    Item it = item;
    do {
      value = func.invoke(qc, info, value, it);
    } while((it = qc.next(iter)) != null);
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1], expr3 = exprs[2];
    if(expr1 == Empty.VALUE) return expr2;

    opt(this, cc, false, true);

    final int limit = cc.qc.context.options.get(MainOptions.UNROLLLIMIT);
    if(expr1 instanceof Value && expr3 instanceof Value && expr1.size() <= limit) {
      // unroll the loop
      Expr expr = expr2;
      for(final Item item : (Value) expr1) {
        expr = new DynFuncCall(info, sc, expr3, expr, item).optimize(cc);
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
          seq.type instanceof ArrayType ? ((ArrayType) seq.type).declType :
            seq.with(Occ.EXACTLY_ONE);

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
