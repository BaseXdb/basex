package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

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
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    ts = ts.compile(ctx, scp);
    // static condition: return branch in question
    if(ts.isValue()) {
      final Value val = ts.value(ctx);
      for(final TypeCase tc : cases) {
        if(tc.matches(val))
          return optPre(tc.compile(ctx, scp, (Value) ts).expr, ctx);
      }
    }
    // compile branches
    for(final TypeCase tc : cases) tc.compile(ctx, scp);

    // return result if all branches are equal (e.g., empty)
    boolean eq = true;
    for(int i = 1; i < cases.length; ++i) {
      eq &= cases[i - 1].expr.sameAs(cases[i].expr);
    }
    if(eq) return optPre(null, ctx);

    type = cases[0].type();
    for(int c = 1; c < cases.length; ++c) {
      type = type.union(cases[c].type());
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value seq = ctx.value(ts);
    for(final TypeCase tc : cases) {
      final Iter iter = tc.iter(ctx, seq);
      if(iter != null) return iter;
    }
    // will never happen
    throw Util.notexpected();
  }

  @Override
  public boolean isVacuous() {
    for(final TypeCase tc : cases) if(!tc.expr.isVacuous()) return false;
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    for(final TypeCase tc : cases) if(tc.uses(u)) return true;
    return ts.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    for(final TypeCase tc : cases) if(!tc.removable(v)) return false;
    return ts.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final TypeCase tc : cases) tc.remove(v);
    ts = ts.remove(v);
    return this;
  }

  @Override
  public VarUsage count(final Var v) {
    return ts.count(v).plus(VarUsage.maximum(v, cases));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    boolean change = inlineAll(ctx, scp, cases, v, e);
    final Expr t = ts.inline(ctx, scp, v, e);
    if(t != null) {
      change = true;
      ts = t;
    }
    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final TypeCase[] cs = new TypeCase[cases.length];
    for(int i = 0; i < cs.length; i++) cs[i] = cases[i].copy(ctx, scp, vs);
    return new TypeSwitch(info, ts.copy(ctx, scp, vs), cs);
  }

  @Override
  public boolean databases(final StringList db) {
    for(final TypeCase tc : cases) if(!tc.databases(db)) return false;
    return ts.databases(db);
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

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return ts.accept(visitor) && visitAll(visitor, cases);
  }
}
