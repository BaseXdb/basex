package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;

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
  public final Expr analyze(final AnalyzeContext ctx) {
    return this;
  }

  @Override
  public Expr compile(final QueryContext ctx) {
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    return false;
  }

  @Override
  public int count(final Var v) {
    return 0;
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return this;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem());
  }
}
