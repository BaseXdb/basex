package org.basex.query.func.async;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author James Wright
 */
public final class ForkJoin extends StandardFunc {
  /** XQuery options. */
  public static class ForkJoinOptions extends Options {
    /** Number of threads to allow in the pool. */
    public static final NumberOption THREADS = new NumberOption("threads",
        Runtime.getRuntime().availableProcessors());
    /** Number of functions to be evaluated by each thread. */
    public static final NumberOption THREAD_SIZE = new NumberOption("thread-size", 1);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value items = qc.value(exprs[0]);
    final ArrayList<FItem> funcs = new ArrayList<>();
    for(final Item item : items) {
      if(item instanceof FItem) {
        final FItem func = (FItem) item;
        if(func.arity() == 0) {
          funcs.add(func);
          continue;
        }
      }
      throw ZEROFUNCS_X_X.get(info, item.type, item);
    }

    final Options opts = new ForkJoinOptions();
    if(exprs.length > 1) toOptions(1, null, opts, qc);
    final int threads = opts.get(ForkJoinOptions.THREADS);
    final int threadSize = opts.get(ForkJoinOptions.THREAD_SIZE);
    if(threadSize < 1) throw ASYNC_ARG_X.get(info, threadSize);

    final ForkJoinPool pool;
    try {
      pool = new ForkJoinPool(threads);
    } catch(final IllegalArgumentException ex) {
      throw ASYNC_ARG_X.get(info, threads);
    }
    final ForkJoinTask<Value> task = new ForkJoinTask<>(funcs, threadSize, qc, info);
    try {
      return pool.invoke(task);
    } catch(final Exception ex) {
      final Throwable e = Util.rootException(ex);
      throw e instanceof QueryException ? (QueryException) e : ASYNC_UNEXP_X.get(info, e);
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
