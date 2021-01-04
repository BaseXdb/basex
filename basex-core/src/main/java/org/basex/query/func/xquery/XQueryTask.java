package org.basex.query.func.xquery;

import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Forks a set of tasks, performing their computation in parallel followed by rejoining the results.
 *
 * @author James Wright
 */
final class XQueryTask extends RecursiveTask<Value> {
  /** Functions to evaluate in parallel. */
  private final Value funcs;
  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo ii;
  /** First function to evaluate. */
  private final int start;
  /** Last function to evaluate. */
  private final int end;

  /**
   * Constructor.
   * @param funcs functions to evaluate
   * @param qc query context
   * @param ii input info
   */
  XQueryTask(final Value funcs, final QueryContext qc, final InputInfo ii) {
    this(funcs, qc, ii, 0, (int) funcs.size());
  }

  /**
   * Private constructor.
   * @param funcs functions to evaluate
   * @param qc query context
   * @param ii input info
   * @param start first function to evaluate
   * @param end last function to evaluate
   */
  private XQueryTask(final Value funcs, final QueryContext qc, final InputInfo ii,
      final int start, final int end) {
    this.funcs = funcs;
    this.qc = new QueryContext(qc);
    this.ii = ii;
    this.start = start;
    this.end = end;
  }

  @Override
  protected Value compute() {
    final ValueBuilder vb = new ValueBuilder(qc);
    final int s = start, e = end, l = e - s;
    if(l == 1) {
      // perform the work
      try {
        vb.add(((FItem) funcs.itemAt(s)).invoke(qc, ii));
      } catch(final QueryException ex) {
        completeExceptionally(ex);
        cancel(true);
      } finally {
        qc.close();
      }
    } else if(l > 1) {
      // split the work and join the results in the correct order
      final int m = s + l / 2;
      final XQueryTask task2 = new XQueryTask(funcs, qc, ii, m, e);
      task2.fork();
      final XQueryTask task1  = new XQueryTask(funcs, qc, ii, s, m);
      vb.add(task1.invoke()).add(task2.join());
    }
    return vb.value();
  }
}
