package org.basex.query.scope;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;

/**
 * Interface for all expressions defining a new variable scope.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public interface Scope {
  /**
   * Traverses this scope with the given {@link ASTVisitor}.
   * @param visitor visitor
   * @return continue flag
   */
  boolean visit(ASTVisitor visitor);

  /**
   * Prepares the scope for compilation.
   */
  default void reset() { }

  /**
   * Compiles the expression contained in this scope.
   * @param cc compilation context
   * @return compiled expression, or {@code null} if not required
   * @throws QueryException compilation errors
   */
  Expr compile(CompileContext cc) throws QueryException;

  /**
   * Checks if this scope has already been compiled.
   * @return result of check
   */
  boolean compiled();
}
