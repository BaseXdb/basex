package org.basex.query.func.random;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Dirk Kirsten
 */
public final class RandomGaussian extends StandardFunc {
  /** Random instance. */
  private static final Random RND = new Random();

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long num = toLong(exprs[0], qc);
    return new Iter() {
      int count;

      @Override
      public Item next() {
        return ++count <= num ? Dbl.get(RND.nextGaussian()) : null;
      }
    };
  }
}
