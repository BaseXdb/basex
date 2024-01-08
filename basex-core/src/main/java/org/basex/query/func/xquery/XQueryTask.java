package org.basex.query.func.xquery;

import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Forks a set of tasks, performing their computation in parallel followed by rejoining the results.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author James Wright
 */
final class XQueryTask extends RecursiveTask<Value> {
  /** Task context. */
  private final TaskContext tc;
  /** First function to evaluate (inclusive). */
  private final int start;
  /** Last function to evaluate (exclusive). */
  private final int end;

  /**
   * Constructor.
   * @param tc task context
   */
  XQueryTask(final TaskContext tc) {
    this(tc, 0, tc.funcs.size());
  }

  /**
   * Private constructor.
   * @param tc task context
   * @param start first function to evaluate
   * @param end last function to evaluate
   */
  private XQueryTask(final TaskContext tc, final int start, final int end) {
    this.tc = tc;
    this.start = start;
    this.end = end;
  }

  @Override
  protected Value compute() {
    final int size = end - start;
    if(size == 1) {
      // perform the work
      try(QueryContext qc = new QueryContext(tc.qc)) {
        return tc.funcs.get(start).invoke(qc, tc.info);
      } catch(final QueryException ex) {
        if(tc.errors) {
          completeExceptionally(ex);
          cancel(true);
        }
      }
    } else {
      // split the work and join the results in the correct order
      final int middle = start + size / 2;
      final XQueryTask task2 = new XQueryTask(tc, middle, end);
      task2.fork();
      final XQueryTask task1 = new XQueryTask(tc, start, middle);
      final Value value1 = task1.invoke(), value2 = task2.join();
      if(tc.results) return ValueBuilder.concat(value1, value2, tc.qc);
    }
    return Empty.VALUE;
  }
}
