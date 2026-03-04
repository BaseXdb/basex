package org.basex.query.func.update;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.up.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdateReplace extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XNode input = toNode(arg(0).item(qc, info));
    final FItem target = toFunction(arg(1), 1, false, qc);
    final FItem data = toFunction(arg(2), 1, false, qc);

    // $input update { for $t in $target(.) return replace node $t with $data($t) }
    final VarScope vs = new VarScope();
    final Var t = vs.add(new Var(new QNm("t"), null, qc, ii));
    final Expr trgt = new DynFuncCall(info, target, new ContextValue(info));
    final For fr = new For(t, null, null, trgt, false);
    final VarRef tref = new VarRef(info, t);
    final Expr dt = new DynFuncCall(info, data, tref);
    final Replace rplc = new Replace(info, tref, dt, false);
    final GFLWOR rtrn = new GFLWOR(info, fr, rplc);
    return new TransformWith(info, input, rtrn).item(qc, info);
  }
}
