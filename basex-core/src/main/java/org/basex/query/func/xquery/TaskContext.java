package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.core.jobs.*;
import org.basex.core.jobs.Job.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Shared context and execution helpers for parallelized queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TaskContext {
  /**
   * A single parallel call: a function with its arguments.
   * @param function function to invoke
   * @param args arguments
   */
  record Call(FItem function, Value[] args) { }

  /** Functional interface for an operation that is run on a pool. */
  @FunctionalInterface
  interface PoolFn {
    /**
     * Runs the operation.
     * @param pool fork/join pool
     * @return result
     * @throws Exception exception
     */
    Value apply(ForkJoinPool pool) throws Exception;
  }

  /** Input info (can be {@code null}). */
  final InputInfo info;
  /** Query context. */
  final QueryContext qc;
  /** Job that groups all parallel branches (allows scoped cancellation). */
  final QueryContext group;
  /** Raise errors. */
  final boolean errors;
  /** Collect results. */
  final boolean results;
  /** Report results and errors. */
  final boolean report;
  /** Maximum number of parallel threads ({@code 0}: shared pool). */
  final int parallel;
  /** Timeout in milliseconds ({@code 0}: no timeout). */
  final long timeout;

  /**
   * Constructor.
   * @param options task options
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  TaskContext(final TaskOptions options, final QueryContext qc, final InputInfo info)
      throws QueryException {
    this.info = info;
    this.qc = qc;
    group = new QueryContext(qc, qc.ns);
    errors = options.get(TaskOptions.ERRORS);
    results = options.get(TaskOptions.RESULTS);
    report = options.get(TaskOptions.REPORT);
    parallel = options.parallel();
    timeout = ((ANum) options.get(TaskOptions.TIMEOUT)).dec(info).
        multiply(BigDecimal.valueOf(1000)).longValue();
  }

  /**
   * Evaluates a function in a parallel branch.
   * @param fn function to evaluate
   * @param cache materialize lazy items of the result (which may raise errors)
   * @return result
   * @throws QueryException query exception
   */
  Value branch(final QueryFunction<QueryContext, Value> fn, final boolean cache)
      throws QueryException {
    // grouped under the job of all branches, with the dynamic namespaces of the original query
    try(QueryContext bqc = new QueryContext(group, qc.ns); Binding bound = bqc.bind()) {
      final Value value = fn.apply(bqc);
      if(cache) value.cache(false, info);
      return value;
    }
  }

  /**
   * Returns the effective level of parallelism.
   * @return number of threads
   */
  int parallelism() {
    return parallel != 0 ? parallel : Runtime.getRuntime().availableProcessors();
  }

  /**
   * Invokes a fork/join task and returns its result.
   * @param task task to invoke
   * @return result
   * @throws QueryException query exception
   */
  Value invoke(final ForkJoinTask<Value> task) throws QueryException {
    return execute(pool -> pool.invoke(task));
  }

  /**
   * Invokes functions in parallel, returns the first successful result and cancels the others.
   * @param functions functions to invoke
   * @return result
   * @throws QueryException query exception
   */
  Value invokeAny(final List<FItem> functions) throws QueryException {
    return execute(pool -> {
      // no ForkJoinPool.invokeAny: its cancellation interrupts threads that may have moved on
      final CompletableFuture<Value> first = new CompletableFuture<>();
      final AtomicInteger open = new AtomicInteger(functions.size());
      for(final FItem function : functions) {
        pool.execute(() -> {
          if(first.isDone()) return;
          try {
            first.complete(branch(bqc -> function.invoke(bqc, info), true));
          } catch(final Throwable th) {
            if(open.decrementAndGet() == 0) first.completeExceptionally(th);
          }
        });
      }
      try {
        return first.get();
      } finally {
        // cancel the losing branches, unless cancellation is already in progress (e.g. a timeout),
        // whose job state must be preserved
        if(!group.stopped()) group.stop();
      }
    });
  }

  /**
   * Wraps a successful result in a report record.
   * @param value result
   * @return record
   */
  XQMap result(final Value value) {
    return XQMap.get(Str.get("value"), value);
  }

  /**
   * Wraps a caught error in a report record. The error is described by the standard error map,
   * the same map that a try/catch clause exposes as {@code $err:map}.
   * @param ex caught exception
   * @return record
   * @throws QueryException query exception
   */
  XQMap error(final QueryException ex) throws QueryException {
    return XQMap.get(Str.get("error"), ex.map());
  }

  /**
   * Sets up a thread pool, runs an operation, and translates and propagates exceptions.
   * @param fn operation
   * @return result
   * @throws QueryException query exception
   */
  private Value execute(final PoolFn fn) throws QueryException {
    // nested calls reuse the pool of the caller instead of waiting for another pool
    final boolean dedicated = parallel != 0;
    final ForkJoinPool caller = ForkJoinTask.getPool();
    final ForkJoinPool pool = dedicated ? new ForkJoinPool(parallel) :
      Objects.requireNonNullElse(caller, ForkJoinPool.commonPool());
    final ScheduledFuture<?> timer = timeout > 0 ?
      qc.context.jobs.schedule(group::timeout, timeout) : null;
    try {
      return dedicated && caller != null ? await(pool, fn) : fn.apply(pool);
    } catch(final Exception ex) {
      // timeout: discard branch errors and report the timeout
      if(group.state == JobState.TIMEOUT) throw XQUERY_TIMEOUT.get(info);
      // stopped caller: report the regular interruption
      qc.checkStop();
      // pass on query and job exceptions
      final Throwable e = Util.rootException(ex);
      if(e instanceof final QueryException qe) throw qe;
      if(e instanceof final JobException je) throw je;
      throw XQUERY_UNEXPECTED_X.get(info, e);
    } finally {
      if(timer != null) timer.cancel(false);
      if(dedicated) pool.shutdown();
      group.close();
    }
  }

  /**
   * Runs an operation on a thread of the specified pool and waits for the result.
   * @param pool fork/join pool
   * @param fn operation
   * @return result
   * @throws InterruptedException interrupted exception
   * @throws ExecutionException execution exception
   */
  private static Value await(final ForkJoinPool pool, final PoolFn fn)
      throws InterruptedException, ExecutionException {
    // plain blocking: a joining worker of another pool would run unrelated tasks meanwhile
    final FutureTask<Value> task = new FutureTask<>(() -> fn.apply(pool));
    pool.execute(task);
    return task.get();
  }
}
