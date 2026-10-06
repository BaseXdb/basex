package org.basex.query.func.random;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.util.Array;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Dirk Kirsten
 */
public final class RandomSeededPermutation extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long seed = toLong(arg(0), qc);
    final Value value = arg(1).value(qc);
    final int size = Array.checkCapacity(value.size());
    final SplittableRandom r = new SplittableRandom(seed);

    // permute positions, build result with compact sequence types
    final int[] positions = new int[size];
    for(int p = 0; p < size; p++) {
      final int l = r.nextInt(p + 1);
      positions[p] = positions[l];
      positions[l] = p;
    }
    final ValueBuilder vb = new ValueBuilder(qc, size);
    for(final int p : positions) vb.add(value.itemAt(p));
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return adoptType(arg(1));
  }
}
