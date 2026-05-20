package org.basex.query.func;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Reusable argument buffer for a higher-order function invocation,
 * with optional positional parameter.
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
  private int pos;

  /**
   * Creates a buffer with {@code arity} slots and no positional parameter.
   * @param arity number of argument slots
   */
  public HofArgs(final int arity) {
    args = new Value[arity];
    posParam = -1;
  }

  /**
   * Creates a buffer sized for the supplied callbacks. If any callback has
   * {@code arity}, the last slot is reserved as positional parameter.
   * @param arity maximum callback arity
   * @param items callback functions (can contain {@code null} references)
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
  public int pos() {
    return pos;
  }

  /**
   * Increases the position.
   * @return self reference
   */
  public HofArgs inc() {
    final int pp = posParam, p = pos + 1;
    if(pp != -1) set(pp, Itr.get(p));
    pos = p;
    return this;
  }
}
