package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFilter extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
   final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    final HofArgs args = new HofArgs(2, predicate);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = input.next()) != null;) {
      if(test(predicate, args.set(0, item).inc(), qc)) vb.add(item);
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), predicate = arg(1);
    final SeqType st = input.seqType();
    if(st.zero()) return input;
    final int arity = arity(predicate);
    if(arity == -1) return this;

    // for $item at $pos in INPUT where PREDICATE($item, $pos) return $item
    final FLWORBuilder flwor = new FLWORBuilder(arity, cc, info);
    final Expr where = flwor.function(this, 1, false);
    final Expr rtrn = flwor.ref(flwor.item);
    return flwor.finish(input, where, rtrn);
  }
}
