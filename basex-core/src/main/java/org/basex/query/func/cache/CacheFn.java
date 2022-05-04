package org.basex.query.func.cache;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * Cache function.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class CacheFn extends StandardFunc {
  /**
   * Returns the key argument.
   * @param qc query context
   * @return key
   * @throws QueryException query exception
   */
  final byte[] toKey(final QueryContext qc) throws QueryException {
    return toToken(exprs[0], qc);
  }

  /**
   * Checks if the specified expression is a valid user name.
   * @param i expression index
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  final String toName(final int i, final QueryContext qc) throws QueryException {
    return toName(i, CACHE_NAME_X, qc);
  }

  /**
   * Returns the state map.
   * @param qc query context
   * @return state map
   */
  final Cache cache(final QueryContext qc) {
    return qc.context.cache;
  }
}
