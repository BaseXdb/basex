package org.basex.query.expr;

import org.basex.query.*;

/**
 * Position checks.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface CmpPos {
  /**
   * Checks the current position.
   * <ul>
   *   <li> Returns {@code 2} if the test is successful and remaining test can be skipped
   *   <li> Returns {@code 1} if the test is successful
   *   <li> Returns {@code 0} otherwise
   * </ul>
   * Should only be called if this is a {@link #simple} check.
   * @param pos current position
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  int test(long pos, QueryContext qc) throws QueryException;

  /**
   * Checks if the positional range is deterministic.
   * @return result of check
   */
  boolean simple();

  /**
   * Checks if minimum and maximum expressions are identical.
   * @return result of check
   */
  boolean exact();

  /**
   * If possible, returns an optimized expression with inverted operands.
   * @param cc compilation context
   * @return original or modified expression
   * @throws QueryException query exception
   */
  Expr invert(CompileContext cc) throws QueryException;
}
