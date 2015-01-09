package org.basex.query;

import org.basex.query.util.*;

/**
 * Interface for all expressions defining a new variable scope.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public interface Scope {
  /**
   * Traverses this scope with the given {@link ASTVisitor}.
   * @param visitor visitor
   * @return continue flag
   */
  boolean visit(final ASTVisitor visitor);

  /**
   * Compiles the expression contained in this scope.
   * @param qc query context
   * @throws QueryException compilation errors
   */
  void compile(QueryContext qc) throws QueryException;

  /**
   * Checks if this scope has already been compiled.
   * @return result of check
   */
  boolean compiled();
}
