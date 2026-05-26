package org.basex.query.func.xquery;

import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Splits a sequence into chunks, folds each chunk sequentially, and merges the partial results
 * with an associative combine function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ReduceTask extends RecursiveTask<Value> {
  /** Task context. */
  private final TaskContext tc;
  /** Input sequence. */
  private final Value input;
  /** Seed value (identity of the combine function). */
  private final Value init;
  /** Action to fold a single item into the accumulator. */
  private final FItem action;
  /** Function to combine two partial results. */
  private final FItem combine;
  /** First item to process (inclusive). */
  private final long start;
  /** Last item to process (exclusive). */
  private final long end;
  /** Maximum chunk size for sequential folding. */
  private final long grain;

  /**
   * Constructor.
   * @param tc task context
   * @param input input sequence
   * @param init seed value
   * @param action fold action
   * @param combine combine function
   */
  ReduceTask(final TaskContext tc, final Value input, final Value init, final FItem action,
      final FItem combine) {
    this(tc, input, init, action, combine, 0, input.size(), grain(tc, input.size()));
  }

  /**
   * Private constructor.
   * @param tc task context
   * @param input input sequence
   * @param init seed value
   * @param action fold action
   * @param combine combine function
   * @param start first item to process
   * @param end last item to process
   * @param grain maximum chunk size
   */
  private ReduceTask(final TaskContext tc, final Value input, final Value init, final FItem action,
      final FItem combine, final long start, final long end, final long grain) {
    this.tc = tc;
    this.input = input;
    this.init = init;
    this.action = action;
    this.combine = combine;
    this.start = start;
    this.end = end;
    this.grain = grain;
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
   * Folds a chunk sequentially or splits it and combines the partial results.
   * @return result
   * @throws QueryException query exception
   */
  private Value computeValue() throws QueryException {
    final long size = end - start;
    if(size <= grain) {
      // fold the chunk sequentially, starting from the seed value
      try(QueryContext qc = tc.context()) {
        Value value = init;
        for(long i = start; i < end; i++) {
          value = action.invoke(qc, tc.info, value, input.itemAt(i));
        }
        return value;
      }
    }
    // split the work and combine the partial results
    final long middle = start + size / 2;
    final ReduceTask task2 = new ReduceTask(tc, input, init, action, combine, middle, end, grain);
    task2.fork();
    final ReduceTask task1 = new ReduceTask(tc, input, init, action, combine, start, middle, grain);
    final Value value1 = task1.invoke(), value2 = task2.join();
    try(QueryContext qc = tc.context()) {
      return combine.invoke(qc, tc.info, value1, value2);
    }
  }

  /**
   * Computes the chunk size (around 4 chunks per thread).
   * @param tc task context
   * @param size input size
   * @return chunk size (at least {@code 1})
   */
  private static long grain(final TaskContext tc, final long size) {
    final long chunks = tc.parallelism() * 4L;
    return Math.max(1, (size + chunks - 1) / chunks);
  }
}
