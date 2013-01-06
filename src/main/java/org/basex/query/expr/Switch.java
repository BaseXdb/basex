package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Switch expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Switch extends ParseExpr {
  /** Cases. */
  private final SwitchCase[] cases;
  /** Condition. */
  private Expr cond;

  /**
   * Constructor.
   * @param ii input info
   * @param c condition
   * @param sc cases (last one is default case)
   */
  public Switch(final InputInfo ii, final Expr c, final SwitchCase[] sc) {
    super(ii);
    cases = sc;
    cond = c;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    for(final SwitchCase sc : cases) sc.checkUp();
    // check if none or all return expressions are updating
    final Expr[] tmp = new Expr[cases.length];
    for(int i = 0; i < tmp.length; ++i) tmp[i] = cases[i].expr[0];
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    cond = cond.compile(ctx, scp);
    for(final SwitchCase sc : cases) sc.compile(ctx, scp);

    // check if expression can be pre-evaluated
    Expr ex = this;
    if(cond.isValue()) {
      final Item it = cond.item(ctx, info);
      LOOP:
      for(final SwitchCase sc : cases) {
        final int sl = sc.expr.length;
        for(int e = 1; e < sl; e++) {
          if(!sc.expr[e].isValue()) break LOOP;

          // includes check for empty sequence (null reference)
          final Item cs = sc.expr[e].item(ctx, info);
          if(it == cs || cs != null && it != null && it.equiv(info, cs)) {
            ex = sc.expr[0];
            break LOOP;
          }
        }
        if(sl == 1) ex = sc.expr[0];
      }
    }
    if(ex != this) return optPre(ex, ctx);

    // expression could not be pre-evaluated
    type = cases[0].expr[0].type();
    for(int c = 1; c < cases.length; c++) {
      type = type.intersect(cases[c].expr[0].type());
    }
    return ex;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(getCase(ctx));
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return ctx.value(getCase(ctx));
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return getCase(ctx).item(ctx, ii);
  }

  @Override
  public boolean uses(final Use u) {
    for(final SwitchCase sc : cases) if(sc.uses(u)) return true;
    return cond.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    for(final SwitchCase sc : cases) if(!sc.removable(v)) return false;
    return cond.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final SwitchCase sc : cases) sc.remove(v);
    cond = cond.remove(v);
    return this;
  }

  @Override
  public boolean databases(final StringList db) {
    for(final SwitchCase sc : cases) if(!sc.databases(db)) return false;
    return cond.databases(db);
  }

  /**
   * Chooses the selected {@code case} expression.
   * @param ctx query context
   * @return case expression
   * @throws QueryException query exception
   */
  private Expr getCase(final QueryContext ctx) throws QueryException {
    final Item it = cond.item(ctx, info);
    for(final SwitchCase sc : cases) {
      final int sl = sc.expr.length;
      for(int e = 1; e < sl; e++) {
        // includes check for empty sequence (null reference)
        final Item cs = sc.expr[e].item(ctx, info);
        if(it == cs || it != null && cs != null && it.equiv(info, cs))
          return sc.expr[0];
      }
      if(sl == 1) return sc.expr[0];
    }
    // will never be evaluated
    return null;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, cases);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(SWITCH + PAR1 + cond + PAR2);
    for(final SwitchCase sc : cases) sb.append(sc.toString());
    return sb.toString();
  }

  @Override
  public Expr markTailCalls() {
    for(final SwitchCase sc : cases) sc.markTailCalls();
    return this;
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return cond.visitVars(visitor) && visitor.visitAll(cases);
  }
}
