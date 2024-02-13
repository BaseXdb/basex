package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Position checks.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public interface CmpPos {
  /**
   * Returns the positions in ascending order.
   * @param qc query context
   * @return positions
   * @throws QueryException query exception
   */
  Value positions(QueryContext qc) throws QueryException;

  /**
   * Checks if the minimum and maximum positions are identical.
   * @return result of check
   */
  boolean exact();

  /**
   * If possible, returns an inverted and optimized position check.
   * @param cc compilation context
   * @return inverted expression or {@code null}
   * @throws QueryException query exception
   */
  Expr invert(CompileContext cc) throws QueryException;
}
