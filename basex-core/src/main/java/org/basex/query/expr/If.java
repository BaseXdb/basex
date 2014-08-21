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
 * @author BaseX Team 2005-14, BSD License
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
    cond = cond.compile(qc, scp).optimizeEbv(qc, scp);
    // static condition: return branch in question
    if(cond.isValue()) return optPre(eval(qc).compile(qc, scp), qc);

    // compile and simplify branches
    final int es = exprs.length;
    for(int e = 0; e < es; e++) {
      try {
        exprs[e] = exprs[e].compile(qc, scp);
      } catch(final QueryException ex) {
        // replace original expression with error
        exprs[e] = FnError.get(ex, seqType);
      }
    }
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // static condition: return branch in question
    if(cond.isValue()) return optPre(eval(qc), qc);

    // if A then B else B -> B (errors in A will be ignored)
    if(exprs[0].sameAs(exprs[1])) return optPre(exprs[0], qc);

    // if not(A) then B else C -> if A then C else B
    if(cond.isFunction(Function.NOT)) {
      qc.compInfo(OPTWRITE, this);
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
          qc.compInfo(OPTPRE, this);
          return compBln(a, info);
        }
        // if(A) then true() else C -> A or C
        qc.compInfo(OPTWRITE, this);
        return new Or(info, a, c).optimize(qc, scp);
      }

      if(c == Bln.TRUE) {
        if(b == Bln.FALSE) {
          // if(A) then false() else true() -> not(A)
          qc.compInfo(OPTPRE, this);
          return Function.NOT.get(null, info, a).optimize(qc, scp);
        }
        // if(A) then B else true() -> not(A) or B
        qc.compInfo(OPTWRITE, this);
        final Expr notA = Function.NOT.get(null, info, a).optimize(qc, scp);
        return new Or(info, notA, b).optimize(qc, scp);
      }

      if(b == Bln.FALSE) {
        // if(A) then false() else C -> not(A) and C
        qc.compInfo(OPTWRITE, this);
        final Expr notA = Function.NOT.get(null, info, a).optimize(qc, scp);
        return new And(info, notA, c).optimize(qc, scp);
      }

      if(c == Bln.FALSE) {
        // if(A) then B else false() -> A and B
        qc.compInfo(OPTWRITE, this);
        return new And(info, a, b).optimize(qc, scp);
      }
    }

    seqType = exprs[0].seqType().union(exprs[1].seqType());
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(eval(qc));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return qc.value(eval(qc));
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return eval(qc).item(qc, info);
  }

  /**
   * Evaluates the condition and returns the matching expression.
   * @param qc query context
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Expr eval(final QueryContext qc) throws QueryException {
    return exprs[cond.ebv(qc, info).bool(info) ? 0 : 1];
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
        nw = FnError.get(qe, seqType);
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
    for(final Expr e : exprs) sz += e.exprSize();
    return sz + cond.exprSize();
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final QueryContext qc, final VarScope scp)
      throws QueryException {
    for(int i = 0; i < exprs.length; i++) {
      final SeqType tp = exprs[i].seqType();
      try {
        exprs[i] = tc.check(exprs[i], qc, scp);
      } catch(final QueryException ex) {
        exprs[i] = FnError.get(ex, tp);
      }
    }
    return optimize(qc, scp);
  }
}
