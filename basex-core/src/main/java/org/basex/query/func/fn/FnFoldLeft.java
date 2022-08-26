package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnFoldLeft extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final Item item = input.next();
    return item != null ? value(input, item, qc) : exprs[1].value(qc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final Item item = input.next();
    return item != null ? value(input, item, qc).iter() : exprs[1].iter(qc);
  }

  /**
   * Evaluates the expression.
   * @param input input iterator
   * @param item first item
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Value value(final Iter input, final Item item, final QueryContext qc)
      throws QueryException {
    Value value = exprs[1].value(qc);
    final FItem action = toFunction(exprs[2], 2, qc);
    Item it = item;
    do {
      value = action.invoke(qc, info, value, it);
    } while((it = input.next()) != null);
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0], zero = exprs[1], action = exprs[2];
    if(input.seqType().zero()) return zero;

    opt(this, cc, false, true);

    // unroll fold
    if(action instanceof Value) {
      final ExprList unroll = cc.unroll(input, true);
      if(unroll != null) {
        Expr expr = zero;
        for(final Expr ex : unroll) {
          expr = new DynFuncCall(info, sc, action, expr, ex).optimize(cc);
        }
        return expr;
      }
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
    final Expr action = exprs[2];
    if(action instanceof FuncItem) {
      // function argument is a single function item
      final SeqType seq = exprs[0].seqType(), zero = exprs[1].seqType(), curr = array &&
          seq.type instanceof ArrayType ? ((ArrayType) seq.type).declType :
            seq.with(Occ.EXACTLY_ONE);

      // assign item type of iterated value, optimize function
      final SeqType[] args = { left ? SeqType.ITEM_ZM : curr, left ? curr : SeqType.ITEM_ZM };
      Expr optFunc = sf.coerceFunc(action, cc, SeqType.ITEM_ZM, args);

      final FuncType ft = optFunc.funcType();
      final int i = left ? 0 : 1;
      SeqType input = zero, output = ft.declType;

      // if initial item has more specific type, assign it and check optimized result type
      final SeqType at = ft.argTypes[i];
      if(!input.eq(at) && input.instanceOf(at)) {
        do {
          args[i] = input;
          optFunc = sf.coerceFunc(action, cc, ft.declType, args);
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
