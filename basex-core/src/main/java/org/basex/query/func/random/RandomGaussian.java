package org.basex.query.func.random;

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
public final class RandomGaussian extends StandardFunc {
  /** Random instance. */
  private static final Random RND = new Random();

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final int num = (int) toLong(exprs[0], qc);
      int count;
      @Override
      public Item next() {
        return ++count <= num ? Dbl.get(RND.nextGaussian()) : null;
      }
    };
  }
}
