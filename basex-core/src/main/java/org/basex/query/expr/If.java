package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * If expression.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /** If expression. */
  private Expr cond;

  /**
   * Constructor.
   * @param info input info
   * @param cond condition
   * @param branch1 then branch
   * @param branch2 else branch
   */
  public If(final InputInfo info, final Expr cond, final Expr branch1, final Expr branch2) {
    super(info, branch1, branch2);
    this.cond = cond;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    checkAllUp(exprs);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    cond = cond.compile(qc, scp);
    // choose branches to compile
    final int[] branches = cond.isValue() ? new int[] { branch(qc) } : new int[] { 0, 1 };
    for(final int b : branches) {
      try {
        exprs[b] = exprs[b].compile(qc, scp);
      } catch (final QueryException ex) {
        // replace original expression with error
        exprs[b] = FnError.get(ex, seqType, scp.sc);
      }
    }
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // static condition: return branch in question
    cond = cond.optimizeEbv(qc, scp);
    if(cond.isValue()) return optPre(exprs[branch(qc)], qc);

    // if A then B else B -> B (errors in A will be ignored)
    if(exprs[0].sameAs(exprs[1])) return optPre(exprs[0], qc);

    // if not(A) then B else C -> if A then C else B
    if(cond.isFunction(Function.NOT)) {
      qc.compInfo(OPTREWRITE_X, this);
      cond = ((Arr) cond).exprs[0];
      final Expr tmp = exprs[0];
      exprs[0] = exprs[1];
      exprs[1] = tmp;
    }

    // rewritings for constant booleans
    if(exprs[0].seqType().eq(SeqType.BLN) && exprs[1].seqType().eq(SeqType.BLN)) {
      final Expr a = cond, b = exprs[0], c = exprs[1];
      if(b == Bln.TRUE) {
        if(c == Bln.FALSE) {
          // if(A) then true() else false() -> xs:boolean(A)
          qc.compInfo(OPTPRE_X, this);
          return compBln(a, info, scp.sc);
        }
        // if(A) then true() else C -> A or C
        qc.compInfo(OPTREWRITE_X, this);
        return new Or(info, a, c).optimize(qc, scp);
      }

      if(c == Bln.TRUE) {
        if(b == Bln.FALSE) {
          // if(A) then false() else true() -> not(A)
          qc.compInfo(OPTPRE_X, this);
          return Function.NOT.get(scp.sc, info, a).optimize(qc, scp);
        }
        // if(A) then B else true() -> not(A) or B
        qc.compInfo(OPTREWRITE_X, this);
        final Expr notA = Function.NOT.get(scp.sc, info, a).optimize(qc, scp);
        return new Or(info, notA, b).optimize(qc, scp);
      }

      if(b == Bln.FALSE) {
        // if(A) then false() else C -> not(A) and C
        qc.compInfo(OPTREWRITE_X, this);
        final Expr notA = Function.NOT.get(scp.sc, info, a).optimize(qc, scp);
        return new And(info, notA, c).optimize(qc, scp);
      }

      if(c == Bln.FALSE) {
        // if(A) then B else false() -> A and B
        qc.compInfo(OPTREWRITE_X, this);
        return new And(info, a, b).optimize(qc, scp);
      }
    }

    seqType = exprs[0].seqType().union(exprs[1].seqType());
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(exprs[branch(qc)]);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return qc.value(exprs[branch(qc)]);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return exprs[branch(qc)].item(qc, info);
  }

  /**
   * Evaluates the condition and returns the offset of the resulting branch.
   * @param qc query context
   * @return branch offset
   * @throws QueryException query exception
   */
  private int branch(final QueryContext qc) throws QueryException {
    return cond.ebv(qc, info).bool(info) ? 0 : 1;
  }

  @Override
  public boolean has(final Flag flag) {
    return cond.has(flag) || super.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    return cond.removable(var) && super.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return cond.count(var).plus(VarUsage.maximum(var, exprs));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {

    final Expr sub = cond.inline(qc, scp, var, ex);
    if(sub != null) cond = sub;
    boolean te = false;
    final int es = exprs.length;
    for(int i = 0; i < es; i++) {
      Expr nw;
      try {
        nw = exprs[i].inline(qc, scp, var, ex);
      } catch(final QueryException qe) {
        nw = FnError.get(qe, seqType, scp.sc);
      }
      if(nw != null) {
        exprs[i] = nw;
        te = true;
      }
    }
    return te || sub != null ? optimize(qc, scp) : null;
  }

  @Override
  public If copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new If(info, cond.copy(qc, scp, vs),
        exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs)));
  }

  @Override
  public boolean isVacuous() {
    return exprs[0].isVacuous() && exprs[1].isVacuous();
  }

  @Override
  public void markTailCalls(final QueryContext qc) {
    exprs[0].markTailCalls(qc);
    exprs[1].markTailCalls(qc);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, exprs);
  }

  @Override
  public String toString() {
    return IF + '(' + cond + ") " + THEN + ' ' + exprs[0] + ' ' + ELSE + ' ' + exprs[1];
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr expr : exprs) sz += expr.exprSize();
    return sz + cond.exprSize();
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final QueryContext qc, final VarScope scp)
      throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      final SeqType tp = exprs[e].seqType();
      try {
        exprs[e] = tc.check(exprs[e], qc, scp);
      } catch(final QueryException ex) {
        exprs[e] = FnError.get(ex, tp, scp.sc);
      }
    }
    return optimize(qc, scp);
  }
}
