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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnFoldLeft extends StandardFunc {
  /** Optimized condition and else branch. */
  private FuncItem[] iff;

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = action(qc);

    final HofArgs args = new HofArgs(2).set(0, arg(1).value(qc));
    for(Item item; (item = input.next()) != null;) {
      args.set(1, item).inc();
      if(skip(qc, args)) break;
      args.set(0, invoke(action, args, qc));
    }
    return args.get(0);
  }

  /**
   * Checks if the evaluation can be exited early.
   * @param qc query context
   * @param args arguments
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean skip(final QueryContext qc, final HofArgs args) throws QueryException {
    return iff != null && test(iff[0], args, qc);
  }

  /**
   * Returns the action.
   * @param qc query context
   * @return action
   * @throws QueryException query exception
   */
  public final FItem action(final QueryContext qc) throws QueryException {
    return iff != null ? iff[1] : toFunction(arg(2), 2, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optType(cc, false, true);
    return expr != this ? expr : unroll(cc, true);
  }

  /**
   * Unrolls the fold.
   * @param cc compilation context
   * @param left indicates if this is a left/right fold
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr unroll(final CompileContext cc, final boolean left) throws QueryException {
    final Expr input = arg(0), init = arg(1);
    final ExprList unroll = cc.unroll(input, true);
    if(unroll != null) {
      final Expr func = coerceFunc(2, cc);
      Expr expr = init;
      if(left) {
        for(final Expr ex : unroll) {
          expr = new DynFuncCall(info, func, expr, ex).optimize(cc);
        }
      } else {
        for(int es = unroll.size() - 1; es >= 0; es--) {
          expr = new DynFuncCall(info, func, unroll.get(es), expr).optimize(cc);
        }
      }
      return expr;
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
  protected final Expr optType(final CompileContext cc, final boolean array, final boolean left)
      throws QueryException {

    final Expr input = arg(0), init = arg(1), action = arg(2);
    final SeqType ist = input.seqType();
    // fold-left((), INIT, $f)  ->  INIT
    if(array ? input == XQArray.empty() : ist.zero()) return init;

    final SeqType zst = init.seqType();
    if(action instanceof final FuncItem fiAction) {
      if(iff == null) {
        final Object fold = fiAction.fold(input, array, left, cc);
        if(fold instanceof final Expr expr) return expr;
        if(fold instanceof String) return init;
        if(fold instanceof final FuncItem[] fi) iff = fi;
      }

      final SeqType i1t = array ? ist.type instanceof final ArrayType at ? at.valueType() :
        SeqType.ITEM_O : ist.with(Occ.EXACTLY_ONE);
      SeqType st = zst, ost;
      do {
        final SeqType[] types = { left ? st : i1t, left ? i1t : st };
        arg(2, arg -> refineFunc(action, cc, types));
        ost = st;
        st = st.union(arg(2).funcType().declType);
      } while(!st.eq(ost));
      exprType.assign(st);
    } else {
      final FuncType ft = action.funcType();
      if(ft != null) exprType.assign(ist.oneOrMore() ? ft.declType : zst.union(ft.declType));
    }
    return this;
  }

  @Override
  public final int hofIndex() {
    return 2;
  }
}
