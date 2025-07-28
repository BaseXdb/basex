package org.basex.query.func.random;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Dirk Kirsten
 */
public final class RandomInteger extends StandardFunc {
  /** Random instance. */
  private static final Random RND = new Random();

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Long max = toLongOrNull(arg(0), qc);
    final long next;
    if(max != null) {
      if(max <= 0 || max > Integer.MAX_VALUE) throw RANDOM_BOUNDS_X.get(info, max);
      next = RND.nextInt((int) (long) max);
    } else {
      next = RND.nextInt();
    }
    return Itr.get(next);
  }
}
