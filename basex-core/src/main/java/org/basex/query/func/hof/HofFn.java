package org.basex.query.func.hof;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Higher-order function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
abstract class HofFn extends StandardFunc {
  /**
   * Gets a comparator from a less-than predicate as function item.
   * The {@link Comparator#compare(Object, Object)} method throws a
   * {@link QueryRTException} if the comparison throws a {@link QueryException}.
   * @param pos argument position of the predicate
   * @param qc query context
   * @return comparator
   * @throws QueryException exception
   */
  final Comparator<Item> getComp(final int pos, final QueryContext qc) throws QueryException {
    final FItem lt = checkArity(exprs[pos], 2, qc);
    return (a, b) -> {
      try {
        return toBoolean(lt.invoke(qc, info, a, b).item(qc, info)) ? -1 : 1;
      } catch(final QueryException qe) {
        throw new QueryRTException(qe);
      }
    };
  }
}
