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
 * @author BaseX Team 2005-15, BSD License
 * @author Dirk Kirsten
 */
public final class RandomSeededDouble extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long seed = toLong(exprs[0], qc);
    final long num = toLong(exprs[1], qc);
    if(num < 0) throw BXRA_NUM_X.get(info, num);

    return new Iter() {
      final Random r = new Random(seed);
      long c;

      @Override
      public Item next() {
        return ++c <= num ? Dbl.get(r.nextDouble()) : null;
      }
    };
  }
}
