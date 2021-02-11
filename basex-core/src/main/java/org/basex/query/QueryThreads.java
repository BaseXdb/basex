package org.basex.query;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.path.*;

/**
 * Local thread caches.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @return cache
   */
  public ThreadLocal<CmpCache> get(final CmpHashG expr) {
    return cmpCache.computeIfAbsent(expr, p -> ThreadLocal.withInitial(CmpCache::new));
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
   * Closes threads.
   */
  void close() {
    for(final ThreadLocal<PathCache> cache : pathCache.values()) cache.remove();
    for(final ThreadLocal<CmpCache> cache : cmpCache.values()) cache.remove();
    for(final ThreadLocal<FTTokenizer> cache : ftCache.values()) cache.remove();
  }
}
