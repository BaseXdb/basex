package org.basex.query.var;

import static org.basex.query.QueryText.*;

import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Superclass for global and local variable references.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class VarRef extends ParseExpr {
  /** Variable name. */
  protected QNm name;

  /**
   * Constructor.
   * @param nm variable name
   * @param ii input info
   */
  protected VarRef(final QNm nm, final InputInfo ii) {
    super(ii);
    name = nm;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR;
  }

  @Override
  public final String toString() {
    return new TokenBuilder(DOLLAR).add(name.string()).toString();
  }
}
