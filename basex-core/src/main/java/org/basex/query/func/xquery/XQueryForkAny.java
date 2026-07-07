package org.basex.query.func.xquery;

import java.util.*;
import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryForkAny extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value functions = arg(0).unwrappedValue(qc);
    final TaskOptions options = toOptions(arg(1), new TaskOptions(), qc);

    final long size = functions.size();
    if(size == 0) return Empty.VALUE;

    final ArrayList<FItem> list = new ArrayList<>(Seq.initialCapacity(size));
    for(final Item function : functions) {
      list.add(checkUp(toFunction(function, 0, qc), false));
    }

    final TaskContext tc = new TaskContext(options, qc, info);
    final ArrayList<Callable<Value>> tasks = new ArrayList<>(list.size());
    for(final FItem function : list) {
      tasks.add(() -> {
        try(QueryContext cqc = tc.context()) {
          return function.invoke(cqc, tc.info);
        }
      });
    }
    return tc.invokeAny(tasks);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr functions = arg(0);
    final SeqType st = functions.seqType();
    if(st.zero()) return functions;
    // xquery:fork-any($function) → $function()
    if(st.one() && arg(1) == Empty.UNDEFINED) {
      return new DynFuncCall(info, coerceFunc(0, cc)).optimize(cc);
    }
    final FuncType ft = functions.funcType();
    if(ft != null) exprType.assign(ft.declType);
    return this;
  }
}
