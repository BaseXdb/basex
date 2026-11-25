package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnStartsWithSubsequence extends FnContainsSubsequence {
  @Override
  Value input(final long size, final QueryContext qc) throws QueryException {
    // only retrieve the leading items (the input may be unbounded)
    return FnSubsequence.prefix(arg(0), size, qc);
  }

  @Override
  boolean match(final Value input, final Value subsequence,
      final QueryBiFunction<Item, Item, Boolean> cmp) throws QueryException {

    return matchAt(input, subsequence, 0, cmp);
  }
}
