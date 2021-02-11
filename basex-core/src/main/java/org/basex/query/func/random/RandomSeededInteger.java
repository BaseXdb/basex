package org.basex.query.func.random;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dirk Kirsten
 */
public final class RandomSeededInteger extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long[] args = args(qc);
    return new Iter() {
      final Random r = new Random(args[0]);
      long c = args[1];
      final int max = (int) args[2];

      @Override
      public Item next() {
        return --c >= 0 ? Int.get(max == 0 ? r.nextInt() : r.nextInt(max)) : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long[] args = args(qc);
    final Random r = new Random(args[0]);
    final int vl = Seq.initialCapacity(args[1]), max = (int) args[2];
    final LongList values = new LongList(vl);
    for(long v = 0; v < vl; v++) values.add(max == 0 ? r.nextInt() : r.nextInt(max));
    return IntSeq.get(values.finish());
  }

  /**
   * Checks and returns the arguments.
   * @param qc query context
   * @return arguments
   * @throws QueryException query exception
   */
  private long[] args(final QueryContext qc) throws QueryException {
    final long seed = toLong(exprs[0], qc);
    final long num = toLong(exprs[1], qc);
    if(num < 0) throw RANGE_NEGATIVE_X.get(info, num);

    long max = 0;
    if(exprs.length > 2) {
      max = toLong(exprs[2], qc);
      if(max < 1 || max > Integer.MAX_VALUE) throw RANDOM_BOUNDS_X.get(info, max);
    }
    return new long[] { seed, num, max };
  }
}
