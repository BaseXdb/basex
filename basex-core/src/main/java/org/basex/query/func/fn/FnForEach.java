package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
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
public class FnForEach extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(1), 2, this instanceof UpdateForEach, qc);

    final HofArgs args = new HofArgs(2, action);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = input.next()) != null;) {
      vb.add(invoke(action, args.set(0, item).inc(), qc));
    }
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), action = arg(1);
    final SeqType st = input.seqType();
    if(st.zero()) return input;
    final int arity = arity(action);
    if(arity == -1) return this;

    // for $item at $pos in INPUT return ACTION($item, $pos)
    final FLWORBuilder flwor = new FLWORBuilder(arity, cc, info);
    final Expr rtrn = flwor.function(this, 1, this instanceof UpdateForEach);
    return flwor.finish(input, null, rtrn);
  }
}
