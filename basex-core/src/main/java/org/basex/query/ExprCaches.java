package org.basex.query;

import java.util.*;
import java.util.function.*;

import org.basex.query.expr.*;

/**
 * Expression caches of a query context.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ExprCaches {
  /** Expression caches. */
  private final IdentityHashMap<Expr, Object> caches = new IdentityHashMap<>();

  /**
   * Returns the cache for the given expression.
   * @param <T> cache type
   * @param expr expression
   * @param cache supplier for a new cache
   * @return cache
   */
  @SuppressWarnings("unchecked")
  public <T> T get(final Expr expr, final Supplier<T> cache) {
    return (T) caches.computeIfAbsent(expr, e -> cache.get());
  }

  /**
   * Removes the cache of the given expression.
   * @param expr expression
   */
  public void remove(final Expr expr) {
    caches.remove(expr);
  }

  /**
   * Removes the caches of all expressions.
   */
  public void clear() {
    if(!caches.isEmpty()) caches.clear();
  }
}
