package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnFoldLeft extends StandardFunc {
  /** Optimized condition and else branch. */
  public FuncItem[] iff;

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = action(qc);

    int p = 0;
    Value result = arg(1).value(qc);
    for(Item item; (item = input.next()) != null;) {
      if(skip(qc, result, item)) break;
      result = action.invoke(qc, info, result, item, Int.get(++p));
    }
    return result;
  }

  /**
   * Checks if the evaluation can be exited early.
   * @param qc query context
   * @param args arguments
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean skip(final QueryContext qc, final Value... args)
      throws QueryException {
    return iff != null && toBoolean(iff[0].invoke(qc, info, args).item(qc, info));
  }

  /**
   * Returns the action.
   * @param qc query context
   * @return action
   * @throws QueryException query exception
   */
  public final FItem action(final QueryContext qc) throws QueryException {
    return iff != null ? iff[1] : toFunction(arg(2), 3, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = optType(cc, false, true);
    if(expr != this) return expr;

    // unroll fold
    final Expr input = arg(0), zero = arg(1), action = arg(2);
    final int al = action.funcType() != null ? action.funcType().argTypes.length : -1;
    if(action instanceof Value && al == 2) {
      final ExprList unroll = cc.unroll(input, true);
      if(unroll != null) {
        final Expr func = coerce(2, cc, al);
        expr = zero;
        for(final Expr ex : unroll) {
          expr = new DynFuncCall(info, sc, func, expr, ex).optimize(cc);
        }
        return expr;
      }
    }
    return this;
  }

  /**
   * Refines the types.
   * @param cc compilation context
   * @param array indicates if an array is processed
   * @param left indicates if this is a left/right fold
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public final Expr optType(final CompileContext cc, final boolean array, final boolean left)
      throws QueryException {

    final Expr input = arg(0), zero = arg(1), action = arg(2);
    final SeqType ist = input.seqType();
    // fold-left((), ZERO, $f)  ->  ZERO
    if(array ? input == XQArray.empty() : ist.zero()) return zero;

    if(action instanceof FuncItem) {
      if(iff == null) {
        final Object fold = ((FuncItem) action).fold(input, array, left, cc);
        if(fold instanceof Expr) return (Expr) fold;
        if(fold instanceof String) return zero;
        if(fold instanceof FuncItem[]) iff = (FuncItem[]) fold;
      }

      // function argument is a single function item
      final SeqType zst = zero.seqType(), curr = array && ist.type instanceof ArrayType ?
        ((ArrayType) ist.type).declType : ist.with(Occ.EXACTLY_ONE);

      // assign item type of iterated value, optimize function
      final SeqType[] types = { left ? SeqType.ITEM_ZM : curr, left ? curr : SeqType.ITEM_ZM,
        SeqType.INTEGER_O };
      Expr optFunc = refineFunc(action, cc, SeqType.ITEM_ZM, types);

      final FuncType ft = optFunc.funcType();
      final int i = left ? 0 : 1;
      SeqType st = zst, output = ft.declType;

      // if initial item has more specific type, assign it and check optimized result type
      if(i < ft.argTypes.length) {
        final SeqType at = ft.argTypes[i];
        if(!st.eq(at) && st.instanceOf(at)) {
          while(true) {
            types[i] = st;
            optFunc = refineFunc(action, cc, ft.declType, types);
            output = optFunc.funcType().declType;

            // optimized type is instance of input type: abort
            if(output.instanceOf(st)) break;
            // combine input and output type, optimize again
            st = st.union(output);
          }
        }
      }

      exprType.assign(!array && ist.oneOrMore() ? output : output.union(zst));
      exprs[2] = optFunc;
    }
    return this;
  }
}
