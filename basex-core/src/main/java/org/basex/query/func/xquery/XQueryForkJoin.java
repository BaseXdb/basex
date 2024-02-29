package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.xquery.TaskOptions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author James Wright
 */
public final class XQueryForkJoin extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter functions = arg(0).iter(qc);
    final TaskOptions options = toOptions(arg(1), new TaskOptions(), true, qc);

    final long size = functions.size();
    if(size == 0) return Empty.VALUE;

    final ArrayList<FItem> list = new ArrayList<>(Seq.initialCapacity(size));
    for(Item function; (function = functions.next()) != null;) {
      list.add(checkUp(toFunction(function, 0, qc), false));
    }
    // single function: invoke directly
    if(list.size() == 1) return list.get(0).invoke(qc, info);

    final ForkJoinPool pool = new ForkJoinPool(options.parallel());
    final TaskContext tc = new TaskContext(list, options, qc, info);
    try {
      return pool.invoke(new XQueryTask(tc));
    } catch(final Exception ex) {
      // pass on query and job exceptions
      final Throwable e = Util.rootException(ex);
      if(e instanceof QueryException) throw (QueryException) e;
      if(e instanceof JobException) throw (JobException) e;
      throw XQUERY_UNEXPECTED_X.get(info, e);
    } finally {
      pool.shutdown();
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr functions = arg(0), options = arg(1);
    final SeqType st = functions.seqType();
    if(st.zero()) return functions;
    if(st.one()) return new DynFuncCall(info, coerce(0, cc)).optimize(cc);

    final Boolean results = options == Empty.UNDEFINED ? Boolean.TRUE :
      options instanceof Value ? toOptions(options, new TaskOptions(), true, cc.qc).get(RESULTS) :
      null;
    if(results == Boolean.TRUE) {
      final FuncType ft = functions.funcType();
      if(ft != null) {
        final SeqType dt = ft.declType;
        exprType.assign(dt.with(dt.occ.multiply(st.occ)));
      }
    } else if(results == Boolean.FALSE) {
      exprType.assign(SeqType.EMPTY_SEQUENCE_Z);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 0;
  }
}
