package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.core.jobs.*;
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
     * @throws Exception any exception
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
   * Creates a query context for a single parallel branch. The context inherits the dynamic
   * namespaces of the original query and is grouped under {@link #group}.
   * @return query context
   */
  QueryContext context() {
    return new QueryContext(group, qc.ns);
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
   * Invokes a set of tasks in parallel and returns the result of the first one that finishes
   * successfully. Remaining branches are cancelled.
   * @param tasks tasks to invoke
   * @return result
   * @throws QueryException query exception
   */
  Value invokeAny(final Collection<Callable<Value>> tasks) throws QueryException {
    return execute(pool -> {
      try {
        return pool.invokeAny(tasks);
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
    final boolean dedicated = parallel != 0;
    final ForkJoinPool pool = dedicated ? new ForkJoinPool(parallel) : ForkJoinPool.commonPool();
    final Timer timer = scheduleTimeout();
    try {
      return fn.apply(pool);
    } catch(final Exception ex) {
      // timeout: discard branch errors and report the timeout
      if(group.state == JobState.TIMEOUT) throw XQUERY_TIMEOUT.get(info);
      // pass on query and job exceptions
      final Throwable e = Util.rootException(ex);
      if(e instanceof final QueryException qe) throw qe;
      if(e instanceof final JobException je) throw je;
      throw XQUERY_UNEXPECTED_X.get(info, e);
    } finally {
      if(timer != null) timer.cancel();
      if(dedicated) pool.shutdown();
      group.close();
    }
  }

  /**
   * Schedules a job that cancels all branches once the timeout is exceeded.
   * @return timer, or {@code null} if no positive timeout was specified
   */
  private Timer scheduleTimeout() {
    if(timeout <= 0) return null;

    final Timer timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        group.timeout();
      }
    }, timeout);
    return timer;
  }
}
