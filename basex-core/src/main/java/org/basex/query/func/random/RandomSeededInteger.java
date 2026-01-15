package org.basex.query.func.random;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Dirk Kirsten
 */
public final class RandomSeededInteger extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long seed = toLong(arg(0), qc);
    final long num = toLong(arg(1), qc);
    final Long max = toLongOrNull(arg(2), qc);
    if(num < 0) throw RANGE_NEGATIVE_X.get(info, num);
    if(max != null && (max < 1 || max > Integer.MAX_VALUE)) throw RANDOM_BOUNDS_X.get(info, max);

    return new Iter() {
      final Random r = new Random(seed);
      final int mx = (int) (max != null ? (long) max : 0);
      long c = num;

      @Override
      public Item next() {
        return --c >= 0 ? Itr.get(mx == 0 ? r.nextInt() : r.nextInt(mx)) : null;
      }
    };
  }
}
