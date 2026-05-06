package org.basex.query.expr;

import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Hash cache for {@link CmpHashG} general comparisons. Items of the right-hand operand are added
 * to a hash set on demand and reused across consecutive evaluations of the same expression.
 * Caching is dismissed if the operand changes and the previous cache turned out to be unused —
 * neither a hit was found nor was the right-hand operand fully consumed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpCache {
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Hash set with items of the right-hand operand; {@code null} if caching has been dismissed. */
  HashItemSet set;
  /** Right-hand operand currently being cached (compared by identity). */
  Value value;
  /** Lazy iterator over the right-hand operand; {@code null} once exhausted. */
  Iter iter;
  /** Indicates if at least one cache hit occurred since the last (re-)initialization. */
  boolean hit;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  public CmpCache(final InputInfo info) {
    this.info = info;
    init();
  }

  /**
   * Activates the cache for the given right-hand operand. Re-initializes the hash set if the
   * operand has changed since the previous call; dismisses the cache permanently if the previous
   * cache had no hits and the right-hand operand was not fully consumed (no reuse benefit).
   * @param val right-hand operand value
   * @param ir right-hand operand iterator
   * @return {@code true} if the cache is active, {@code false} if the caller should fall back to
   *   a non-cached comparison
   */
  boolean active(final Value val, final Iter ir) {
    // check if caching was dismissed
    if(set == null) return false;

    // operand changed: dismiss only if cache was unused (no hits AND not fully built)
    if(value != val) {
      if(!hit && iter != null) {
        set = null;
        value = null;
        iter = null;
        return false;
      }
      init();
      value = val;
      iter = ir;
      hit = false;
    }
    return true;
  }

  /**
   * Creates a fresh empty hash set.
   */
  private void init() {
    set = new HashItemSet(ItemSet.Mode.EQUAL, info);
  }
}