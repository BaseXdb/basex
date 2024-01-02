package org.basex.query.func.random;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Dirk Kirsten
 */
public final class RandomGaussian extends StandardFunc {
  /** Random instance. */
  private static final Random RND = new Random();

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long count = toLong(arg(0), qc);
    return new Iter() {
      long c = count;

      @Override
      public Item next() {
        return --c >= 0 ? Dbl.get(RND.nextGaussian()) : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long count = toLong(arg(0), qc);
    final DoubleList values = new DoubleList(Seq.initialCapacity(count));
    for(long c = 0; c < count; c++) values.add(RND.nextGaussian());
    return DblSeq.get(values.finish());
  }
}
