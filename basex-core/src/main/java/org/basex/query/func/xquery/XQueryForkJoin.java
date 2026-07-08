package org.basex.query.func.xquery;

import static org.basex.query.func.xquery.TaskOptions.*;

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
    final TaskOptions options = toOptions(arg(1), new TaskOptions(), qc);

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
    final Expr functions = arg(0), options = arg(1);
    final SeqType st = functions.seqType();
    if(st.zero()) return functions;
    // xquery:fork-join($function) → $function()
    if(st.one() && options == Empty.UNDEFINED) {
      return new DynFuncCall(info, coerceFunc(0, cc)).optimize(cc);
    }

    final Boolean results = options == Empty.UNDEFINED ? Boolean.TRUE :
      options instanceof Value ? toOptions(options, new TaskOptions(), cc.qc).get(RESULTS) :
      null;
    if(results == Boolean.TRUE) {
      final FuncType ft = functions.funcType();
      if(ft != null) {
        final SeqType dt = ft.refinedType;
        exprType.assign(dt.with(dt.occ.multiply(st.occ)));
      }
    } else if(results == Boolean.FALSE) {
      exprType.assign(Types.EMPTY_SEQUENCE_Z);
    }
    return this;
  }
}
