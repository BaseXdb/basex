package org.basex.query.var;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Variable stack.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class VarStack extends ObjectList<Var, VarStack> {
  /**
   * Default constructor.
   */
  public VarStack() {
    super(new Var[Array.INITIAL_CAPACITY]);
  }

  /**
   * Returns a variable with the specified name; should only be
   * used while parsing, because it ignores the ids of variables.
   * @param name variable name
   * @return variable or {@code null}
   */
  public Var get(final QNm name) {
    for(int l = size; l-- > 0;) {
      final Var var = list[l];
      if(name.eq(var.name)) return var;
    }
    return null;
  }

  @Override
  protected Var[] newArray(final int s) {
    return new Var[s];
  }
}
