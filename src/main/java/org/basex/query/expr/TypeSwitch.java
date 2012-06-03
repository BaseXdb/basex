package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Typeswitch expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TypeSwitch extends ParseExpr {
  /** Cases. */
  private final TypeCase[] cases;
  /** Condition. */
  private Expr ts;

  /**
   * Constructor.
   * @param ii input info
   * @param t typeswitch expression
   * @param c case expressions
   */
  public TypeSwitch(final InputInfo ii, final Expr t, final TypeCase[] c) {
    super(ii);
    ts = t;
    cases = c;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(ts);
    final Expr[] tmp = new Expr[cases.length];
    for(int i = 0; i < cases.length; ++i) tmp[i] = cases[i].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr analyze(final AnalyzeContext ctx) throws QueryException {
    ts = ts.analyze(ctx);
    for(final TypeCase c : cases) c.analyze(ctx);
    return this;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    ts = ts.compile(ctx);
    // static condition: return branch in question
    if(ts.isValue()) {
      for(final TypeCase c : cases) {
        if(c.var.type == null || c.var.type.instance(ts.value(ctx)))
          return optPre(c.compile(ctx, (Value) ts).expr, ctx);
      }
    }

    // compile branches
    for(final TypeCase c : cases) c.compile(ctx);

    // return result if all branches are equal (e.g., empty)
    boolean eq = true;
    for(int i = 1; i < cases.length; ++i) {
      eq &= cases[i - 1].expr.sameAs(cases[i].expr);
    }
    if(eq) return optPre(null, ctx);

    // evaluate return type
    type = cases[0].type();
    for(int c = 1; c < cases.length; ++c) {
      type = type.intersect(cases[c].type());
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value seq = ctx.value(ts);
    for(final TypeCase c : cases) {
      final Iter iter = c.iter(ctx, seq);
      if(iter != null) return iter;
    }
    // will never happen
    throw Util.notexpected();
  }

  @Override
  public boolean isVacuous() {
    for(final TypeCase c : cases) if(!c.expr.isVacuous()) return false;
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    if(u == Use.VAR) return true;
    for(final TypeCase c : cases) if(c.uses(u)) return true;
    return ts.uses(u);
  }

  @Override
  public int count(final Var v) {
    int c = ts.count(v);
    for(final TypeCase t : cases) c += t.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    for(final TypeCase c : cases) if(!c.removable(v)) return false;
    return ts.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final TypeCase c : cases) c.remove(v);
    ts = ts.remove(v);
    return this;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cases, ts);
  }

  @Override
  public String toString() {
    return new TokenBuilder(TYPESWITCH + PAR1 + ts + PAR2 + ' ').addSep(
        cases, SEP).toString();
  }

  @Override
  public Expr markTailCalls() {
    for(final TypeCase t : cases) t.markTailCalls();
    return this;
  }
}
