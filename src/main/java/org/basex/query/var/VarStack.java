package org.basex.query.var;

import java.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Variable stack.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class VarStack {
  /** Variable expressions. */
  public Var[] vars;
  /** Number of stored variables. */
  public int size;

  /**
   * Default constructor.
   */
  public VarStack() {
    this(4);
  }

  /**
   * Default constructor.
   * @param c initial capacity
   */
  public VarStack(final int c) {
    vars = new Var[c];
  }

  /**
   * Adds the specified variable.
   * @param v variable
   */
  public void push(final Var v) {
    if(size == vars.length) vars = Arrays.copyOf(vars, Array.newSize(size));
    vars[size++] = v;
  }

  /**
   * Returns a variable with the specified name; should only be
   * used while parsing because it ignores ids of variables.
   * @param name variable name
   * @return variable
   */
  public Var get(final QNm name) {
    for(int i = size; i-- > 0;) if(name.eq(vars[i].name)) return vars[i];
    return null;
  }

  /**
   * Returns the index of a variable with the same id, or {@code -1}.
   * @param v variable
   * @return index
   */
  private int indexOf(final Var v) {
    for(int s = size - 1; s >= 0; s--) if(v.is(vars[s])) return s;
    return -1;
  }

  /**
   * Checks if the given variable is in this list.
   * @param v variable
   * @return {@code true} if the variable was found, {@code false} otherwise
   */
  public boolean contains(final Var v) {
    return indexOf(v) != -1;
  }

  /** Empties this stack. */
  public void clear() {
    Arrays.fill(vars, null);
    size = 0;
  }
}
