package org.basex.query.func.xquery;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.xquery.TaskContext.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author James Wright
 */
public final class XQueryForkJoin extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value functions = arg(0).unwrappedValue(qc);
    final TaskOptions options = options(1, TaskOptions::new, qc);

    final long size = functions.size();
    if(size == 0) return Empty.VALUE;

    final Value[] noargs = {};
    final ArrayList<Call> calls = new ArrayList<>(Seq.initialCapacity(size));
    for(final Item function : functions) {
      calls.add(new Call(checkUp(toFunction(function, 0, qc), false), noargs));
    }

    final TaskContext tc = new TaskContext(options, qc, info);
    return tc.invoke(new XQueryTask(tc, calls));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr functions = arg(0);
    final SeqType st = functions.seqType();
    // xquery:fork-join(()) → ()
    if(st.zero()) return functions;
    // xquery:fork-join($function) → $function()
    if(st.one() && !defined(1)) {
      return new DynFuncCall(info, coerceFunc(0, cc)).optimize(cc);
    }

    optOptions(1, TaskOptions::new, cc);
    if(arg(1) instanceof Value) {
      final SeqType type = options(1, TaskOptions::new, cc.qc).type(functions.funcType(), st.occ);
      if(type != null) exprType.assign(type);
    }
    return this;
  }
}
