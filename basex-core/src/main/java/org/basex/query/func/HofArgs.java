package org.basex.query.func;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Encapsulation of higher-order functions arguments.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HofArgs {
  /** Arguments. */
  private final Value[] args;
  /** Index of positional parameter. */
  private final int posParam;
  /** Current position. */
  private long pos;

  /**
   * Constructor.
   * @param arity function arity
   */
  public HofArgs(final int arity) {
    args = new Value[arity];
    posParam = -1;
  }

  /**
   * Constructor.
   * @param arity function arity
   * @param items higher-order functions that may contain positional parameters
   *   (can contain {@code null} references)
   */
  public HofArgs(final int arity, final FItem... items) {
    Boolean p = null;
    for(final FItem item : items) {
      if(item == null) continue;
      if(item.arity() == arity) {
        p = Boolean.TRUE;
      } else if(p == null) {
        p = Boolean.FALSE;
      }
    }
    args = new Value[p == Boolean.FALSE ? arity - 1 : arity];
    posParam = p == Boolean.FALSE ? -1 : arity - 1;
  }

  /**
   * Constructor.
   * @param args arguments
   */
  public HofArgs(final Value... args) {
    this.args = args;
    posParam = -1;
  }

  /**
   * Returns the arguments.
   * @return arguments
   */
  public Value[] get() {
    return args;
  }

  /**
   * Returns the specified argument value.
   * @param index index
   * @return argument
   */
  public Value get(final int index) {
    return args[index];
  }

  /**
   * Assigns an argument.
   * @param index index
   * @param value value
   * @return self reference
   */
  public HofArgs set(final int index, final Value value) {
    args[index] = value;
    return this;
  }

  /**
   * Returns the current position.
   * @return position
   */
  public long pos() {
    return pos;
  }

  /**
   * Increases the position.
   * @return self reference
   */
  public HofArgs inc() {
    final int pp = posParam;
    final long p = pos + 1;
    if(pp != -1) set(pp, Int.get(p));
    pos = p;
    return this;
  }
}
