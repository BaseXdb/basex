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
 * @author BaseX Team 2005-18, BSD License
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
    super(info, SeqType.ITEM_ZM, branch1, branch2);
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
    final int[] branches = cond instanceof Value ? new int[] { branch(cc.qc) } : new int[] { 0, 1 };
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
    if(cond instanceof Value) return cc.replaceWith(this, exprs[branch(cc.qc)]);

    // if A then B else B -> B (errors in A will be ignored)
    if(exprs[0].equals(exprs[1])) return cc.replaceWith(this, exprs[0]);

    // if not(A) then B else C -> if A then C else B
    if(cond.isFunction(Function.NOT)) {
      cc.info(OPTSWAP_X, this);
      cond = ((Arr) cond).exprs[0];
      final Expr tmp = exprs[0];
      exprs[0] = exprs[1];
      exprs[1] = tmp;
    }

    // rewritings for constant booleans
    final SeqType st1 = exprs[0].seqType(), st2 = exprs[1].seqType();
    if(st1.eq(SeqType.BLN_O) && st2.eq(SeqType.BLN_O)) {
      final Expr a = cond, b = exprs[0], c = exprs[1];
      if(b == Bln.TRUE) {
        // if(A) then true() else false() -> xs:boolean(A)
        if(c == Bln.FALSE) return cc.replaceWith(this, FnBoolean.get(a, info, cc.sc()));
        // if(A) then true() else C -> A or C
        return cc.replaceWith(this, new Or(info, a, c).optimize(cc));
      }

      if(c == Bln.TRUE) {
        // if(A) then false() else true() -> not(A)
        if(b == Bln.FALSE) return cc.replaceWith(this, cc.function(Function.NOT, info, a));
        // if(A) then B else true() -> not(A) or B
        final Expr expr = new Or(info, cc.function(Function.NOT, info, a), b).optimize(cc);
        return cc.replaceWith(this, expr);
      }

      // if(A) then false() else C -> not(A) and C
      if(b == Bln.FALSE) {
        final Expr expr = new And(info, cc.function(Function.NOT, info, a), c).optimize(cc);
        return cc.replaceWith(this, expr);
      }

      // if(A) then B else false() -> A and B
      if(c == Bln.FALSE) return cc.replaceWith(this, new And(info, a, b).optimize(cc));
    }

    exprType.assign(st1.union(st2));
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return exprs[branch(qc)].iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs[branch(qc)].value(qc);
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
  public boolean has(final Flag... flags) {
    return cond.has(flags) || super.has(flags);
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
    boolean changed = false;
    final Expr sub = cond.inline(var, ex, cc);
    if(sub != null) {
      cond = sub;
      changed = true;
    }
    final int es = exprs.length;
    for(int e = 0; e < es; e++) {
      Expr exp;
      try {
        exp = exprs[e].inline(var, ex, cc);
      } catch(final QueryException qe) {
        exp = cc.error(qe, this);
      }
      if(exp != null) {
        exprs[e] = exp;
        changed = true;
      }
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public If copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new If(info, cond.copy(cc, vm), exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean isVacuous() {
    return exprs[0].isVacuous() && exprs[1].isVacuous();
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final Expr expr : exprs) expr.markTailCalls(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int size = cond.exprSize();
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      Expr expr = exprs[e];
      try {
        expr = tc.check(expr, cc);
      } catch(final QueryException qe) {
        expr = cc.error(qe, expr);
      }
      if(expr != exprs[e]) {
        changed = true;
        exprs[e] = expr;
      }
    }
    return changed ? optimize(cc) : this;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof If && cond.equals(((If) obj).cond) && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, exprs);
  }

  @Override
  public String toString() {
    return IF + '(' + cond + ") " + THEN + ' ' + exprs[0] + ' ' + ELSE + ' ' + exprs[1];
  }
}
