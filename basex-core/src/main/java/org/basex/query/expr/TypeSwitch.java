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

/**
 * Typeswitch expression.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class TypeSwitch extends ParseExpr {
  /** Cases. */
  private final TypeCase[] cases;
  /** Condition. */
  private Expr ts;

  /**
   * Constructor.
   * @param info input info
   * @param ts typeswitch expression
   * @param cases case expressions
   */
  public TypeSwitch(final InputInfo info, final Expr ts, final TypeCase[] cases) {
    super(info);
    this.ts = ts;
    this.cases = cases;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(ts);
    final int cl = cases.length;
    final Expr[] tmp = new Expr[cl];
    for(int c = 0; c < cl; ++c) tmp[c] = cases[c].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    ts = ts.compile(qc, scp);
    // static condition: return branch in question
    if(ts.isValue()) {
      final Value val = ts.value(qc);
      for(final TypeCase tc : cases) {
        if(tc.matches(val)) return optPre(tc.compile(qc, scp, (Value) ts).expr, qc);
      }
    }
    // compile branches
    for(final TypeCase tc : cases) tc.compile(qc, scp);

    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) {
    // return first branch if all branches are equal (e.g., empty) and use no variables
    final TypeCase tc = cases[0];
    boolean eq = tc.var == null;
    final int cl = cases.length;
    for(int c = 1; eq && c < cl; c++) eq = tc.expr.sameAs(cases[c].expr);
    if(eq) return optPre(tc.expr, qc);

    // combine return types
    seqType = cases[0].seqType();
    for(int c = 1; c < cl; c++) seqType = seqType.union(cases[c].seqType());
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value seq = qc.value(ts);
    for(final TypeCase tc : cases) {
      final Iter iter = tc.iter(qc, seq);
      if(iter != null) return iter;
    }
    // will never happen
    throw Util.notExpected();
  }

  @Override
  public boolean isVacuous() {
    for(final TypeCase tc : cases) if(!tc.expr.isVacuous()) return false;
    return true;
  }

  @Override
  public boolean has(final Flag flag) {
    for(final TypeCase tc : cases) if(tc.has(flag)) return true;
    return ts.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    for(final TypeCase tc : cases) if(!tc.removable(var)) return false;
    return ts.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return ts.count(var).plus(VarUsage.maximum(var, cases));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {

    boolean change = inlineAll(qc, scp, cases, var, ex);
    final Expr t = ts.inline(qc, scp, var, ex);
    if(t != null) {
      change = true;
      ts = t;
    }
    return change ? optimize(qc, scp) : null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new TypeSwitch(info, ts.copy(qc, scp, vs), Arr.copyAll(qc, scp, vs, cases));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cases, ts);
  }

  @Override
  public String toString() {
    return new TokenBuilder(TYPESWITCH + PAREN1 + ts + PAREN2 + ' ').addSep(cases, " ").
        toString();
  }

  @Override
  public void markTailCalls(final QueryContext qc) {
    for(final TypeCase t : cases) t.markTailCalls(qc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return ts.accept(visitor) && visitAll(visitor, cases);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : cases) sz += e.exprSize();
    return sz + ts.exprSize();
  }
}
