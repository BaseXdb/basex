package org.basex.query.flwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Grouping specification.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class GroupSpec extends Single {
  /** Grouping variable. */
  public final Var var;
  /** This contains an assignment. */
  public final boolean assign;

  /**
   * Constructor.
   * @param ii input info
   * @param gv grouping variable
   * @param e grouping expression
   * @param a assign
   */
  public GroupSpec(final InputInfo ii, final Var gv, final Expr e, final boolean a) {
    super(ii, e);
    var = gv;
    assign = a;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return value(ctx).item(ctx, ii);
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final Value val = expr.value(ctx);
    if(val.size() > 1) throw Err.XGRP.thrw(info);
    return val.isEmpty() ? val : StandardFunc.atom(val.itemAt(0), info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), var, expr);
  }

  @Override
  public String toString() {
    return var + " " + ASSIGN + ' ' + expr;
  }
}
