package org.basex.query.expr;

import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;

/**
 * Cached items.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CmpCache {
  /** Cached items. */
  HashItemSet set = new HashItemSet(true);
  /** Cached value (right-hand operand). */
  private Value value;
  /** Lazy iterator (right-hand operand). */
  Iter iter;
  /** Cache hits. */
  int hits = Integer.MAX_VALUE;

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
      set = new HashItemSet(true);
      value = val;
      iter = ir;
      hits = 0;
    }
    return true;
  }
}