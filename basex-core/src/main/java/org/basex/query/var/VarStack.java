package org.basex.query.var;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Variable stack.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class VarStack extends ElementList {
  /** Variable expressions. */
  private Var[] vars;

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
    if(size == vars.length) vars = Array.copy(vars, new Var[newSize()]);
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
}
