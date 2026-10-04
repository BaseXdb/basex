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
    final long count = toLong(arg(1), qc);
    final Long max = toLongOrNull(arg(2), qc);
    if(count < 0) throw RANGE_NEGATIVE_X.get(info, count);
    if(max != null && max < 1) throw RANDOM_BOUNDS_X.get(info, max);

    return new Iter() {
      final SplittableRandom r = new SplittableRandom(seed);
      final long mx = max != null ? max : 0;
      long c = count;

      @Override
      public Item next() {
        if(--c < 0) return null;
        // int bounds: keep sequences of existing seeds
        return Itr.get(mx == 0 ? r.nextInt() : mx <= Integer.MAX_VALUE ? r.nextInt((int) mx) :
          r.nextLong(mx));
      }
    };
  }
}
