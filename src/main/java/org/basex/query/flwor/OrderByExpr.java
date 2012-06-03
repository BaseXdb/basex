package org.basex.query.flwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Single order specifier.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class OrderByExpr extends OrderBy {
  /** Order expression. */
  private Expr expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param d descending order
   * @param l least empty order
   */
  public OrderByExpr(final InputInfo ii, final Expr e, final boolean d,
      final boolean l) {
    super(ii);
    expr = e;
    desc = d;
    lst = l;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public OrderByExpr analyze(final AnalyzeContext ctx) throws QueryException {
    expr = expr.analyze(ctx);
    return this;
  }

  @Override
  public OrderByExpr compile(final QueryContext ctx) throws QueryException {
    expr = expr.compile(ctx);
    type = expr.type();
    return this;
  }

  @Override
  Item key(final QueryContext ctx, final int i) throws QueryException {
    Item it = expr.item(ctx, info);
    if(it != null) {
      if(it.type.isNode()) it = Str.get(it.string(info));
      else if(it.type.isNumber() && Double.isNaN(it.dbl(info))) it = null;
    }
    return it;
  }

  @Override
  public boolean uses(final Use u) {
    return expr.uses(u);
  }

  @Override
  public int count(final Var v) {
    return expr.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public OrderByExpr remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DIR, desc ? DESCENDING : ASCENDING,
        EMPTYORD, lst ? LEAST : GREATEST), expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(expr.toString());
    if(desc) sb.append(' ' + DESCENDING);
    if(!lst) sb.append(' ' + EMPTYORD + ' ' + GREATEST);
    return sb.toString();
  }
}
