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
    public static final NumberOption SPLIT = new NumberOption("split", 1);
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

    Options opts = new ForkJoinOptions();
    if(exprs.length > 2) opts = toOptions(1, null, opts, qc);
    final int threads = opts.get(ForkJoinOptions.THREADS);
    final int split = opts.get(ForkJoinOptions.SPLIT);

    final ForkJoinPool pool = new ForkJoinPool(threads);
    final ForkJoinTask<Value> task = new ForkJoinTask<>(funcs, split, qc, info);
    try {
      return pool.invoke(task);
    } catch(final Exception ex) {
      final Throwable e = Util.rootException(ex);
      throw e instanceof QueryException ? (QueryException) e : ASYNC_ERR_X.get(info, e);
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
