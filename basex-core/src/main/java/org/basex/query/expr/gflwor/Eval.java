package org.basex.query.expr.gflwor;

import org.basex.query.*;

/**
 * Evaluator for FLWOR clauses.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
abstract class Eval {
  /**
   * Makes the next evaluation step if available. This method is guaranteed
   * to not be called again if it has once returned {@code false}.
   * @param qc query context
   * @return {@code true} if step was made, {@code false} if no more results exist
   * @throws QueryException evaluation exception
   */
  abstract boolean next(QueryContext qc) throws QueryException;
}
