package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnEvery extends StandardFunc {
  @Override
  public final Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // implementation for dynamic function lookup
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public final boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    // implementation for dynamic function lookup
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunctionOrNull(arg(1), 2, qc);

    final HofArgs args = predicate != null ? new HofArgs(2, predicate) : null;
    final boolean some = some();
    for(Item item; (item = input.next()) != null;) {
      final boolean test = predicate == null ? item.test(qc, ii, 0) :
        invoke(predicate, args.set(0, item).inc(), qc).test(qc, info, 0);
      if(test == some) return some;
    }
    return !some;
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), predicate = arg(1);
    final SeqType st = input.seqType();
    final boolean some = some();
    if(st.zero()) return cc.voidAndReturn(input, Bln.get(!some), info);
    final boolean pred = defined(1);
    final int arity = pred ? arity(predicate) : 1;
    if(arity == -1) return this;

    // FLWOR: for $item at $pos in INPUT return PREDICATE($item, $pos)  (or: boolean($item))
    final FLWORBuilder flwor = new FLWORBuilder(arity, cc, info);
    final Expr rtrn = pred ? flwor.function(this, 1, false) :
      cc.function(Function.BOOLEAN, info, flwor.refs());
    final Expr expr = flwor.finish(input, null, rtrn);

    // some : FLWOR = true()
    // every: not(FLWOR = false())
    final Expr cmp = new CmpG(info, expr, Bln.get(some), CmpOp.EQ).optimize(cc);
    return some ? cmp : cc.function(Function.NOT, info, cmp);
  }

  /**
   * Compare some/all results.
   * @return flag
   */
  boolean some() {
    return false;
  }
}
