package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnStartsWithSubsequence extends FnContainsSubsequence {
  @Override
  boolean compare(final Value input, final Value subsequence,
      final QueryBiFunction<Item, Item, Boolean> cmp) throws QueryException {

    final long is = input.size(), ss = subsequence.size();
    if(is < ss) return false;

    for(long s = 0; s < ss; s++) {
      if(!cmp.apply(input.itemAt(s), subsequence.itemAt(s))) return false;
    }
    return true;
  }
}
