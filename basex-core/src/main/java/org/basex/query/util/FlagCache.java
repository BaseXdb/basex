package org.basex.query.util;

import java.util.*;
import java.util.function.*;

import org.basex.query.expr.*;

/**
 * Cached compiler properties of a declaration that may reference itself.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FlagCache {
  /** Invalidations of false properties that were computed within an enclosing computation. */
  private static final ThreadLocal<ArrayList<Runnable>> NESTED = new ThreadLocal<>();

  /** Cached properties. */
  private final EnumMap<Flag, Boolean> props = new EnumMap<>(Flag.class);
  /** Computes a property. */
  private final Predicate<Flag> compute;

  /**
   * Constructor.
   * @param compute computes a property
   */
  public FlagCache(final Predicate<Flag> compute) {
    this.compute = compute;
  }

  /**
   * Checks if one of the specified properties applies, and computes missing properties.
   * @param flags flags
   * @return result of check
   * @see Expr#has(Flag...)
   */
  public boolean has(final Flag... flags) {
    boolean missing = false;
    for(final Flag flag : flags) {
      final Boolean prop = props.get(flag);
      if(prop == null) missing = true;
      else if(prop) return true;
    }
    if(!missing) return false;

    for(final Flag flag : flags) {
      if(has(flag)) return true;
    }
    return false;
  }

  /**
   * Checks if the specified property applies, and computes it if it is missing.
   * @param flag flag
   * @return result of check
   */
  private boolean has(final Flag flag) {
    // recursive references: properties that are currently computed are assumed to be false
    final Boolean cached = props.putIfAbsent(flag, Boolean.FALSE);
    if(cached != null) return cached;

    ArrayList<Runnable> nested = NESTED.get();
    final boolean outermost = nested == null;
    if(outermost) NESTED.set(nested = new ArrayList<>());
    final boolean prop;
    try {
      prop = compute.test(flag);
    } finally {
      if(outermost) NESTED.remove();
    }
    props.put(flag, prop);
    if(outermost) {
      // nested results may rely on assumptions that turned out to be wrong
      if(prop) nested.forEach(Runnable::run);
    } else if(!prop) {
      nested.add(() -> props.remove(flag));
    }
    return prop;
  }

  /**
   * Invalidates all cached properties.
   */
  public void clear() {
    props.clear();
  }
}
