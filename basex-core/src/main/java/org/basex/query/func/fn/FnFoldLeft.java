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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnFoldLeft extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Item item = input.next();
    return item != null ? value(input, item, qc) : arg(1).value(qc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Item item = input.next();
    return item != null ? value(input, item, qc).iter() : arg(1).iter(qc);
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
    Value value = arg(1).value(qc);
    final FItem action = toFunction(arg(2), 2, qc);
    Item it = item;
    do {
      value = action.invoke(qc, info, value, it);
    } while((it = input.next()) != null);
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), zero = arg(1), action = arg(2);
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

    final Expr[] args = sf.args();
    final Expr action = args[2];
    if(action instanceof FuncItem) {
      // function argument is a single function item
      final SeqType seq = args[0].seqType(), zero = args[1].seqType(), curr = array &&
          seq.type instanceof ArrayType ? ((ArrayType) seq.type).declType :
            seq.with(Occ.EXACTLY_ONE);

      // assign item type of iterated value, optimize function
      final SeqType[] st = { left ? SeqType.ITEM_ZM : curr, left ? curr : SeqType.ITEM_ZM };
      Expr optFunc = sf.coerceFunc(action, cc, SeqType.ITEM_ZM, st);

      final FuncType ft = optFunc.funcType();
      final int i = left ? 0 : 1;
      SeqType input = zero, output = ft.declType;

      // if initial item has more specific type, assign it and check optimized result type
      final SeqType at = ft.argTypes[i];
      if(!input.eq(at) && input.instanceOf(at)) {
        while(true) {
          st[i] = input;
          optFunc = sf.coerceFunc(action, cc, ft.declType, st);
          output = optFunc.funcType().declType;

          // optimized type is instance of input type: abort
          if(output.instanceOf(input)) break;
          // combine input and output type, optimize again
          input = input.union(output);
        }
      }

      sf.exprType.assign(array || !seq.oneOrMore() ? output.union(zero) : output);
      args[2] = optFunc;
    }
  }
}
