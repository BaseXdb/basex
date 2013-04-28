package org.basex.query;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Superclass for static functions, variables and the main expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticScope extends ExprInfo implements Scope {
  /** Variable scope. */
  protected final VarScope scope;
  /** Compilation flag. */
  protected boolean compiled;
  /** Input info. */
  public final InputInfo info;
  /** Root expression of this declaration. */
  public Expr expr;

  /**
   * Constructor.
   * @param scp variable scope
   * @param ii input info
   */
  public StaticScope(final VarScope scp, final InputInfo ii) {
    scope = scp;
    info = ii;
  }

  @Override
  public final boolean compiled() {
    return compiled;
  }
}
