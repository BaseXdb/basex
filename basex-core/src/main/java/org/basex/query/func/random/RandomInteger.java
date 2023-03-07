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
 * @author BaseX Team 2005-23, BSD License
 * @author Dirk Kirsten
 */
public final class RandomInteger extends StandardFunc {
  /** Random instance. */
  private static final Random RND = new Random();

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long next;
    if(defined(0)) {
      final long max = toLong(exprs[0], qc);
      if(max <= 0 || max > Integer.MAX_VALUE) throw RANDOM_BOUNDS_X.get(info, max);
      next = RND.nextInt((int) max);
    } else {
      next = RND.nextInt();
    }
    return Int.get(next);
  }
}
