package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-17, BSD License
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
  public Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    // choose branches to compile
    final int[] branches = cond.isValue() ? new int[] { branch(cc.qc) } : new int[] { 0, 1 };
    for(final int b : branches) {
      try {
        exprs[b] = exprs[b].compile(cc);
      } catch (final QueryException ex) {
        // replace original expression with error
        exprs[b] = cc.error(ex, this);
      }
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // static condition: return branch in question
    cond = cond.optimizeEbv(cc);
    if(cond.isValue()) return optPre(exprs[branch(cc.qc)], cc);

    // if A then B else B -> B (errors in A will be ignored)
    if(exprs[0].sameAs(exprs[1])) return optPre(exprs[0], cc);

    // if not(A) then B else C -> if A then C else B
    if(cond.isFunction(Function.NOT)) {
      cc.info(OPTREWRITE_X, this);
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
          cc.info(OPTPRE_X, this);
          return compBln(a, info, cc.sc());
        }
        // if(A) then true() else C -> A or C
        cc.info(OPTREWRITE_X, this);
        return new Or(info, a, c).optimize(cc);
      }

      if(c == Bln.TRUE) {
        if(b == Bln.FALSE) {
          // if(A) then false() else true() -> not(A)
          cc.info(OPTPRE_X, this);
          return cc.function(Function.NOT, info, a);
        }
        // if(A) then B else true() -> not(A) or B
        cc.info(OPTREWRITE_X, this);
        return new Or(info, cc.function(Function.NOT, info, a), b).optimize(cc);
      }

      if(b == Bln.FALSE) {
        // if(A) then false() else C -> not(A) and C
        cc.info(OPTREWRITE_X, this);
        return new And(info, cc.function(Function.NOT, info, a), c).optimize(cc);
      }

      if(c == Bln.FALSE) {
        // if(A) then B else false() -> A and B
        cc.info(OPTREWRITE_X, this);
        return new And(info, a, b).optimize(cc);
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
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    final Expr sub = cond.inline(var, ex, cc);
    if(sub != null) cond = sub;
    boolean te = false;
    final int es = exprs.length;
    for(int i = 0; i < es; i++) {
      Expr nw;
      try {
        nw = exprs[i].inline(var, ex, cc);
      } catch(final QueryException qe) {
        nw = cc.error(qe, this);
      }
      if(nw != null) {
        exprs[i] = nw;
        te = true;
      }
    }
    return te || sub != null ? optimize(cc) : null;
  }

  @Override
  public If copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new If(info, cond.copy(cc, vm),
        exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean isVacuous() {
    return exprs[0].isVacuous() && exprs[1].isVacuous();
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    exprs[0].markTailCalls(cc);
    exprs[1].markTailCalls(cc);
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
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      try {
        exprs[e] = tc.check(exprs[e], cc);
      } catch(final QueryException ex) {
        exprs[e] = cc.error(ex, exprs[e]);
      }
    }
    return optimize(cc);
  }
}
