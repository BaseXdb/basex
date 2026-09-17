package org.basex.query.func.random;

import static org.basex.query.QueryError.*;

import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Dirk Kirsten
 */
public final class RandomInteger extends StandardFunc {
  @Override
  public Itr value(final QueryContext qc) throws QueryException {
    final Long max = toLongOrNull(arg(0), qc);
    final long next;
    if(max != null) {
      if(max <= 0 || max > Integer.MAX_VALUE) throw RANDOM_BOUNDS_X.get(info, max);
      next = ThreadLocalRandom.current().nextInt((int) (long) max);
    } else {
      next = ThreadLocalRandom.current().nextInt();
    }
    return Itr.get(next);
  }
}
