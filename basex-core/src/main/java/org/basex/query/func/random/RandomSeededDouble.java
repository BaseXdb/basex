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
public final class RandomSeededDouble extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long seed = toLong(arg(0), qc);
    final long count = toLong(arg(1), qc);
    if(count < 0) throw RANGE_NEGATIVE_X.get(info, count);

    return new Iter() {
      final Random r = new Random(seed);
      long c = count;

      @Override
      public Item next() {
        return --c >= 0 ? Dbl.get(r.nextDouble()) : null;
      }
    };
  }
}
