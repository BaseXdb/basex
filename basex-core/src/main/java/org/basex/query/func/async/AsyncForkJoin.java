package org.basex.query.func.async;

import static org.basex.query.QueryError.*;

import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author James Wright
 */
public final class AsyncForkJoin extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value funcs = qc.value(exprs[0]);
    for(final Item func : funcs) {
      if(!(func instanceof FItem) || ((FItem) func).arity() != 0)
        throw ZEROFUNCS_X_X.get(info, func.type, func);
    }

    final ForkJoinPool pool = new ForkJoinPool();
    final ForkJoinTask task = new ForkJoinTask(funcs, qc, info);
    try {
      return pool.invoke(task);
    } catch(final Exception ex) {
      final Throwable e = Util.rootException(ex);
      throw e instanceof QueryException ? (QueryException) e : ASYNC_UNEXPECTED_X.get(info, e);
    } finally {
      // required?
      pool.shutdown();
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
