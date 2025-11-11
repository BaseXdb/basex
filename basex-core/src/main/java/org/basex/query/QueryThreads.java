package org.basex.query;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Local thread caches.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryThreads {
  /** Path caches. */
  private final IdentityHashMap<AxisPath, ThreadLocal<PathCache>> pathCache =
      new IdentityHashMap<>();
  /** Comparison caches. */
  private final IdentityHashMap<CmpHashG, ThreadLocal<CmpCache>> cmpCache =
      new IdentityHashMap<>();
  /** Full-text tokenizers. */
  private final IdentityHashMap<FTWords, ThreadLocal<FTTokenizer>> ftCache =
      new IdentityHashMap<>();
  /** Per-thread cache for modules compiled by fn:load-xquery-module). */
  private final ThreadLocal<Map<String, XQMap>> moduleCache = ThreadLocal.withInitial(HashMap::new);

  /**
   * Returns local thread for the given expression.
   * @param expr expression
   * @return cache
   */
  public ThreadLocal<PathCache> get(final AxisPath expr) {
    return pathCache.computeIfAbsent(expr, p -> ThreadLocal.withInitial(PathCache::new));
  }

  /**
   * Returns local thread for the given expression.
   * @param expr expression
   * @param info input info (can be {@code null})
   * @return cache
   */
  public ThreadLocal<CmpCache> get(final CmpHashG expr, final InputInfo info) {
    return cmpCache.computeIfAbsent(expr, p -> ThreadLocal.withInitial(() -> new CmpCache(info)));
  }

  /**
   * Returns local thread for the given expression.
   * @param expr expression
   * @return cache
   */
  public ThreadLocal<FTTokenizer> get(final FTWords expr) {
    return ftCache.computeIfAbsent(expr, p -> new ThreadLocal<>());
  }

  /**
   * Returns the per-thread compiled-result cache.
   * @return compiled module cache
   */
  public ThreadLocal<Map<String, XQMap>> moduleCache() {
    return moduleCache;
  }

  /**
   * Closes threads.
   */
  void close() {
    for(final ThreadLocal<PathCache> cache : pathCache.values()) cache.remove();
    for(final ThreadLocal<CmpCache> cache : cmpCache.values()) cache.remove();
    for(final ThreadLocal<FTTokenizer> cache : ftCache.values()) cache.remove();
    moduleCache.remove();
  }
}
