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

    // handle recursive references: properties that are currently computed are assumed to be false
    final ArrayList<Flag> list = new ArrayList<>(flags.length);
    for(final Flag flag : flags) {
      if(props.putIfAbsent(flag, Boolean.FALSE) == null) list.add(flag);
    }
    boolean has = false;
    for(final Flag flag : list) {
      final boolean prop = compute.test(flag);
      props.put(flag, prop);
      has |= prop;
    }
    return has;
  }

  /**
   * Invalidates all cached properties.
   */
  public void clear() {
    props.clear();
  }
}
