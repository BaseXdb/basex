package org.basex.query.expr;

import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Cached items.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CmpCache {
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Cached items. */
  HashItemSet set;
  /** Cached value (right-hand operand). */
  private Value value;
  /** Lazy iterator (right-hand operand). */
  Iter iter;
  /** Cache hits. */
  int hits = Integer.MAX_VALUE;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  public CmpCache(final InputInfo info) {
    this.info = info;
    init();
  }

  /**
   * Checks if caching is enabled. Assigns values required for caching.
   * @param val value
   * @param ir iterator
   * @return result of check
   */
  boolean active(final Value val, final Iter ir) {
    // check if caching was dismissed
    if(set == null) return false;

    // check if this is the first call, of if the value to be cached has changed
    if(value != val) {
      // no cache hits: dismiss caching
      if(hits < 1) {
        set = null;
        value = null;
        iter = null;
        return false;
      }
      // create new cache
      init();
      value = val;
      iter = ir;
      hits = 0;
    }
    return true;
  }

  /**
   * Initializes the hash item set.
   */
  private void init() {
    set = new HashItemSet(true, info);
  }
}