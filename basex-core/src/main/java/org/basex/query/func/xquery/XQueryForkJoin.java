package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author James Wright
 */
public final class XQueryForkJoin extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    final long size = value.size();
    if(size == 0) return Empty.VALUE;

    final ArrayList<FItem> funcs = new ArrayList<>((int) size);
    for(final Item func : value) {
      if(!(func instanceof FItem) || ((FItem) func).arity() != 0)
        throw ZEROFUNCS_X_X.get(info, func.type, func);
      funcs.add(checkUp((FItem) func, false, sc));
    }
    // single function: invoke directly
    if(size == 1) return funcs.get(0).invoke(qc, info);

    final ForkJoinPool pool = new ForkJoinPool();
    final XQueryTask task = new XQueryTask(funcs, qc, info);
    try {
      return pool.invoke(task).value(this);
    } catch(final Exception ex) {
      // pass on query and job exceptions
      final Throwable e = Util.rootException(ex);
      if(e instanceof QueryException) throw (QueryException) e;
      if(e instanceof JobException) throw (JobException) e;
      throw XQUERY_UNEXPECTED_X.get(info, e);
    } finally {
      // required?
      pool.shutdown();
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final SeqType st = exprs[0].seqType();
    if(st.zero()) return exprs[0];
    if(st.one()) return new DynFuncCall(info, sc, exprs[0]).optimize(cc);

    final Type type = st.type;
    if(type instanceof FuncType) {
      final SeqType dt = ((FuncType) type).declType;
      exprType.assign(dt.with(dt.occ.multiply(st.occ)));
    }
    return this;
  }
}
