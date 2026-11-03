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
  /** Caches of expressions and types. */
  private final IdentityHashMap<Object, Object> caches = new IdentityHashMap<>();
  /** Per-thread cache for modules compiled by fn:load-xquery-module (read by parallel tasks). */
  private final ThreadLocal<Map<String, XQMap>> moduleCache = ThreadLocal.withInitial(HashMap::new);

  /**
   * Returns the cache for the given expression or type.
   * @param <T> cache type
   * @param key expression or type
   * @param cache supplier for a new cache
   * @return cache
   */
  @SuppressWarnings("unchecked")
  public <T> T get(final Object key, final Supplier<T> cache) {
    Object c = caches.get(key);
    if(c == null) {
      c = cache.get();
      caches.put(key, c);
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
   * Removes the caches of all expressions.
   */
  public void clear() {
    if(!caches.isEmpty()) caches.clear();
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
