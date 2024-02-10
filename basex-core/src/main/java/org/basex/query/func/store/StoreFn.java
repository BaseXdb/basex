package org.basex.query.func.store;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;

/**
 * Store function.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class StoreFn extends StandardFunc {
  /**
   * Returns the key argument.
   * @param qc query context
   * @return key
   * @throws QueryException query exception
   */
  final byte[] toKey(final QueryContext qc) throws QueryException {
    return toToken(arg(0), qc);
  }

  /**
   * Evaluates an expression to a store name.
   * @param expr expression
   * @param qc query context
   * @return store name
   * @throws QueryException query exception
   */
  final String toName(final Expr expr, final QueryContext qc) throws QueryException {
    return toName(expr, false, STORE_NAME_X, qc);
  }

  /**
   * Returns the store.
   * @param qc query context
   * @return state map
   */
  static Store store(final QueryContext qc) {
    return qc.context.store;
  }
}
