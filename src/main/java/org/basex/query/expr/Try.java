package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project specific try/catch expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Try extends Single {
  /** Catches. */
  private final Catch[] ctch;

  /**
   * Constructor.
   * @param ii input info
   * @param t try expression
   * @param c catch expressions
   */
  public Try(final InputInfo ii, final Expr t, final Catch[] c) {
    super(ii, t);
    ctch = c;
  }

  @Override
  public void checkUp() throws QueryException {
    // check if none or all try/catch expressions are updating
    final Expr[] tmp = new Expr[ctch.length + 1];
    tmp[0] = expr;
    for(int c = 0; c < ctch.length; ++c) tmp[c + 1] = ctch[c].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    try {
      super.compile(ctx, scp);
      if(expr.isValue()) return optPre(expr, ctx);
    } catch(final QueryException ex) {
      // replace original return expression with error
      expr = FNInfo.error(ex, info);
    }

    for(final Catch c : ctch) c.compile(ctx, scp);
    type = expr.type();
    for(final Catch c : ctch) type = type.intersect(c.type());
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    // don't catch errors from error handlers
    try {
      return ctx.value(expr);
    } catch(final QueryException ex) {
      for(final Catch c : ctch) {
        final Value val = c.value(ctx, ex);
        if(val != null) return val;
      }
      throw ex;
    }
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final boolean ct = inlineAll(ctx, scp, ctch, v, e);
    final Expr sub = expr.inline(ctx, scp, v, e);
    if(sub != null) expr = sub;
    return ct || sub != null ? optimize(ctx, scp) : null;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Catch c : ctch) if(c.uses(u)) return true;
    return super.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Catch c : ctch) if(!c.removable(v)) return false;
    return super.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final Catch c : ctch) c.remove(v);
    return super.remove(v);
  }

  @Override
  public boolean databases(final StringList db) {
    for(final Catch c : ctch) if(!c.databases(db)) return false;
    return super.databases(db);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, ctch);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("try { " + expr + " }");
    for(final Catch c : ctch) sb.append(' ').append(c);
    return sb.toString();
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return expr.visitVars(visitor) && visitor.visitAll(ctch);
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.maximum(v, ctch).plus(super.count(v));
  }
}
