package org.basex.query.var;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Variable stack.
 *
 * @author BaseX Team, BSD License
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

  @Override
  protected Var[] newArray(final int s) {
    return new Var[s];
  }
}
