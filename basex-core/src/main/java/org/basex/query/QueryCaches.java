package org.basex.query;

import java.util.*;
import java.util.function.*;

import org.basex.query.expr.*;
import org.basex.query.value.map.*;

/**
 * Caches of a query context.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryCaches {
  /** Expression caches. */
  private final IdentityHashMap<Expr, Object> caches = new IdentityHashMap<>();
  /** Per-thread cache for modules compiled by fn:load-xquery-module (read by parallel tasks). */
  private final ThreadLocal<Map<String, XQMap>> moduleCache = ThreadLocal.withInitial(HashMap::new);

  /**
   * Returns the cache for the given expression.
   * @param <T> cache type
   * @param expr expression
   * @param cache supplier for a new cache
   * @return cache
   */
  @SuppressWarnings("unchecked")
  public <T> T get(final Expr expr, final Supplier<T> cache) {
    Object c = caches.get(expr);
    if(c == null) {
      c = cache.get();
      caches.put(expr, c);
    }
    return (T) c;
  }

  /**
   * Removes the cache of the given expression.
   * @param expr expression
   */
  public void remove(final Expr expr) {
    caches.remove(expr);
  }

  /**
   * Returns the per-thread compiled-result cache.
   * @return compiled module cache
   */
  public ThreadLocal<Map<String, XQMap>> moduleCache() {
    return moduleCache;
  }

  /**
   * Closes the caches.
   */
  void close() {
    caches.clear();
    moduleCache.remove();
  }
}
