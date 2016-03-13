package org.basex.query.func.async;

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
public final class ForkJoinTask extends RecursiveTask<Value> {
  /** Functions to evaluate in parallel. */
  private final Value funcs;
  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo ii;
  /** Split value. */
  private final int split;
  /** First function to evaluate. */
  private final int start;
  /** Last function to evaluate. */
  private final int end;

  /**
   * Constructor.
   * @param funcs functions to evaluate
   * @param split split factor
   * @param qc query context
   * @param ii input info
   */
  public ForkJoinTask(final Value funcs, final int split, final QueryContext qc,
      final InputInfo ii) {
    this(funcs, split, qc, ii, 0, (int) funcs.size());
  }

  /**
   * Private constructor.
   * @param funcs functions to evaluate
   * @param split split factor
   * @param qc query context
   * @param ii input info
   * @param start first function to evaluate
   * @param end last function to evaluate
   */
  private ForkJoinTask(final Value funcs, final int split, final QueryContext qc,
      final InputInfo ii, final int start, final int end) {
    this.funcs = funcs;
    this.split = split;
    this.qc = new QueryContext(qc);
    this.ii = ii;
    this.start = start;
    this.end = end;
  }

  @Override
  protected Value compute() {
    final ValueBuilder vb = new ValueBuilder();
    if(end - start <= split) {
      // perform the work
      try {
        final int last = Math.min(end, start + split);
        for(int f = start; f < last; f++) {
          vb.add(((FItem) funcs.itemAt(f)).invokeValue(qc, ii));
        }
      } catch(final QueryException ex) {
        completeExceptionally(ex);
        cancel(true);
      }
    } else {
      // split the work and join the results in the correct order
      final int mid = start + (end - start) / 2;
      final ForkJoinTask first  = new ForkJoinTask(funcs, split, qc, ii, start, mid);
      final ForkJoinTask second = new ForkJoinTask(funcs, split, qc, ii, mid, end);
      invokeAll(first, second);
      vb.add(first.join()).add(second.join());
    }
    return vb.value();
  }
}
