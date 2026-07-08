package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryReduce extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final Value init = arg(1).value(qc);
    final FItem action = toFunction(arg(2), 2, qc);
    final FItem combine = toFunction(arg(3), 2, qc);
    final TaskOptions options = toOptions(arg(4), new TaskOptions(), qc);

    if(input.size() == 0) return init;

    final TaskContext tc = new TaskContext(options, qc, info);
    return tc.invoke(new ReduceTask(tc, input, init, action, combine));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = arg(0), init = arg(1);
    if(input.seqType().zero()) return init;
    // result type: union of the seed type and the declared action/combine types
    SeqType st = init.seqType();
    final FuncType at = arg(2).funcType(), ct = arg(3).funcType();
    if(at != null) st = st.union(at.refinedType);
    if(ct != null) st = st.union(ct.refinedType);
    exprType.assign(st);
    return this;
  }
}
