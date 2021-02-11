package org.basex.query.expr.path;

import org.basex.query.expr.*;
import org.basex.query.func.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Cache for path results.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class PathCache {
  /** Caching states. */
  enum State {
    /** Initialize caching.  */ INIT,
    /** Caching is possible. */ ENABLED,
    /** Ready to cache.      */ READY,
    /** Results are cached.  */ CACHED,
    /** Caching is disabled. */ DISABLED
  }

  /** Current state. */
  State state = State.INIT;
  /** Cached result. */
  Value result;
  /** Initial context value. */
  Value initial;

  /**
   * Checks if the specified context value is different to the cached one.
   * @param value current context value
   * @param root root expression
   * @return result of check
   */
  boolean sameContext(final Value value, final Expr root) {
    // check if context value has changed, or if roots of cached and new context value are equal
    return value == initial || (
      root instanceof UtilRoot && ((UtilRoot) root).exprs[0] instanceof ContextValue &&
      value instanceof ANode && initial instanceof ANode &&
      ((ANode) initial).root().equals(((ANode) value).root())
    );
  }
}
