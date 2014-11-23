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
 * @author BaseX Team 2005-14, BSD License
 * @author Dirk Kirsten
 */
public final class RandomSeededInteger extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long seed = toLong(exprs[0], qc);
    final long num = toLong(exprs[1], qc);
    if(num < 0) throw BXRA_NUM_X.get(info, num);
    final long max = exprs.length > 2 ? toLong(exprs[2], qc) : Integer.MAX_VALUE;
    if(max < 1 || max > Integer.MAX_VALUE) throw BXRA_BOUNDS_X.get(info, max);

    return new Iter() {
      final Random r = new Random(seed);
      long c;

      @Override
      public Item next() {
        return ++c <= num ? Int.get(
            max == Integer.MAX_VALUE ? r.nextInt() : r.nextInt((int) max)) : null;
      }
    };
  }
}
