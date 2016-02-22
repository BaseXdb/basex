package org.basex.query.func.async;

import java.util.*;
import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Forks a set of tasks, performing their computation in parallel followed by rejoining the results.
 *
 * @author James Wright
 * @param <T> value type
 */
public final class ForkJoinTask<T extends Value> extends RecursiveTask<Value>
  implements Callable<T> {

  /** Functions to evaluate in parallel. */
  private ArrayList<FItem> funcs;
  /** Query context. */
  private QueryContext qc;
  /** Input info. */
  private InputInfo ii;
  /** Split value. */
  private int split;
  /** First function to evaluate. */
  private int start;
  /** Last function to evaluate. */
  private int end;

  /**
   * Constructor.
   * @param funcs functions to evaluate
   * @param split split factor
   * @param qc query context
   * @param ii input info
   */
  public ForkJoinTask(final ArrayList<FItem> funcs, final int split, final QueryContext qc,
      final InputInfo ii) {
    this(funcs, split, qc, ii, 0, funcs.size());
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
  private ForkJoinTask(final ArrayList<FItem> funcs, final int split, final QueryContext qc,
      final InputInfo ii, final int start, final int end) {
    this.funcs = funcs;
    this.split = split;
    this.qc = new QueryContext(qc);
    this.ii = ii;
    this.start = start;
    this.end = end;
  }

  @Override
  public T call() throws Exception {
    return compute();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T compute() {
    final ValueBuilder vb = new ValueBuilder();
    final int length = end - start;
    if(length <= split) {
      // perform the work
      try {
        for(int i = start, j = 0; i < end && j < split; i++, j++) {
          vb.add(funcs.get(i).invokeValue(qc, ii));
        }
      } catch(final QueryException ex) {
        completeExceptionally(ex);
        cancel(true);
      }
    } else {
      // split the work
      final int spl = length / 2;
      ForkJoinTask<Value> second;
      second = new ForkJoinTask<>(funcs, split, qc, ii, start + spl, end);
      end = start + spl;

      second.fork();
      vb.add(compute());
      vb.add(second.join());
    }
    return (T) vb.value();
  }
}
