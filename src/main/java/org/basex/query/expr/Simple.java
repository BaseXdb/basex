package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Simple expression without arguments.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Simple extends ParseExpr {
  /**
   * Constructor.
   * @param ii input info
   */
  protected Simple(final InputInfo ii) {
    super(ii);
  }

  @Override
  public final void checkUp() {
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) {
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.NEVER;
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return null;
  }

  @Override
  public boolean databases(final StringList db) {
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem());
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return true;
  }
}
