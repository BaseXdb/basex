package org.basex.query.var;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Variable stack.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class VarStack extends ObjectList<Var, VarStack> {
  /**
   * Default constructor.
   */
  public VarStack() {
    this(Array.CAPACITY);
  }

  /**
   * Default constructor.
   * @param c initial capacity
   */
  public VarStack(final int c) {
    super(new Var[c]);
  }

  /**
   * Returns a variable with the specified name; should only be
   * used while parsing because it ignores ids of variables.
   * @param name variable name
   * @return variable
   */
  public Var get(final QNm name) {
    for(int i = size; i-- > 0;) if(name.eq(list[i].name)) return list[i];
    return null;
  }

  @Override
  protected Var[] newList(final int s) {
    return new Var[s];
  }
}
