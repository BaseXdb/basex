package org.basex.query.func.xquery;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.jobs.Job.*;
import org.basex.query.*;
import org.basex.query.func.xquery.TaskContext.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Forks a set of calls, performing their computation in parallel followed by rejoining the
 * results in input order.
 *
 * @author BaseX Team, BSD License
 * @author James Wright
 */
final class XQueryTask extends RecursiveTask<Value> {
  /** Task context. */
  private final TaskContext tc;
  /** Calls to evaluate. */
  private final List<Call> calls;
  /** First call to evaluate (inclusive). */
  private final int start;
  /** Last call to evaluate (exclusive). */
  private final int end;

  /**
   * Constructor.
   * @param tc task context
   * @param calls calls to evaluate
   */
  XQueryTask(final TaskContext tc, final List<Call> calls) {
    this(tc, calls, 0, calls.size());
  }

  /**
   * Private constructor.
   * @param tc task context
   * @param calls calls to evaluate
   * @param start first call to evaluate
   * @param end last call to evaluate
   */
  private XQueryTask(final TaskContext tc, final List<Call> calls, final int start,
      final int end) {
    this.tc = tc;
    this.calls = calls;
    this.start = start;
    this.end = end;
  }

  @Override
  protected Value compute() {
    try {
      return computeValue();
    } catch(final QueryException ex) {
      completeExceptionally(ex);
      cancel(true);
      return Empty.VALUE;
    }
  }

  /**
   * Performs the computation, splitting the work and rejoining the results in order.
   * @return result
   * @throws QueryException query exception
   */
  private Value computeValue() throws QueryException {
    final int size = end - start;
    if(size == 1) {
      // perform the work in an isolated query context
      final Call call = calls.get(start);
      try(QueryContext qc = tc.context(); Binding bound = qc.bind()) {
        final Value value = call.function().invoke(qc, tc.info, call.args());
        return tc.report ? tc.result(value) : tc.results ? value : Empty.VALUE;
      } catch(final QueryException ex) {
        if(tc.report) return tc.error(ex);
        if(tc.errors) throw ex;
        return Empty.VALUE;
      }
    }
    // split the work and join the results in the correct order
    final int middle = start + size / 2;
    final XQueryTask task2 = new XQueryTask(tc, calls, middle, end);
    task2.fork();
    final XQueryTask task1 = new XQueryTask(tc, calls, start, middle);
    final Value value1 = task1.invoke(), value2 = task2.join();
    return tc.results || tc.report ? value1.append(value2, tc.qc) : Empty.VALUE;
  }
}
