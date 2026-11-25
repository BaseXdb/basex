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
public final class FnEndsWithSubsequence extends FnContainsSubsequence {
  @Override
  boolean match(final Value input, final Value subsequence,
      final QueryBiFunction<Item, Item, Boolean> cmp) throws QueryException {

    return matchAt(input, subsequence, input.size() - subsequence.size(), cmp);
  }
}
