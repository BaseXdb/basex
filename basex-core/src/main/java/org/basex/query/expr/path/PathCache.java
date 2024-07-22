package org.basex.query.expr.path;

import java.util.function.*;

import org.basex.query.expr.*;
import org.basex.query.func.util.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Cache for path results.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class PathCache {
  /** Caching states. */
  enum State {
    /** Initialize caching.  */ INIT,
    /** Caching is enabled.  */ ENABLED,
    /** Results are cached.  */ CACHED,
    /** Caching is disabled. */ DISABLED
  }

  /** Current state. */
  State state = State.INIT;
  /** Function to check if the context has changed. */
  Predicate<Value> test;
  /** Cached context. */
  Value context;
  /** Cached result. */
  Value result;

  /**
   * Initializes the cache.
   * @param value context value
   * @param path path reference
   */
  void init(final Value value, final AxisPath path) {
    if(!path.hasFreeVars() && !path.has(Flag.NDT)) {
      update(value, null);
      final Expr root = path.root;
      if(root instanceof UtilRoot && ((UtilRoot) root).exprs[0] instanceof ContextValue &&
          context instanceof ANode) {
        test = v -> v instanceof ANode && ((ANode) v).root().equals(((ANode) context).root());
      } else if(root != null && !root.has(Flag.CTX)) {
        test = v -> true;
      } else if(!(value instanceof DBNode)) {
        test = v -> v == context;
      }
    }
    state = test != null ? State.ENABLED : State.DISABLED;
  }

  /**
   * Checks if the cache is valid for the given context value.
   * @param value context value
   * @return result of check
   */
  boolean valid(final Value value) {
    return test.test(value);
  }

  /**
   * Caches the result.
   * @param res result
   */
  void cache(final Value res) {
    result = res;
    state = State.CACHED;
  }

  /**
   * Updates the context value and result.
   * @param value context value
   * @param res result
   */
  void update(final Value value, final Value res) {
    context = value instanceof DBNode ? ((DBNode) value).finish() : value;
    result = res;
  }

  /**
   * Disables the cache.
   */
  void disable() {
    context = null;
    result = null;
    state = State.DISABLED;
  }
}
