package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Switch expression.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class Switch extends ParseExpr {
  /** Cases. */
  private SwitchCase[] cases;
  /** Condition. */
  private Expr cond;

  /**
   * Constructor.
   * @param info input info
   * @param cond condition
   * @param cases cases (last one is default case)
   */
  public Switch(final InputInfo info, final Expr cond, final SwitchCase[] cases) {
    super(info);
    this.cases = cases;
    this.cond = cond;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    for(final SwitchCase sc : cases) sc.checkUp();
    // check if none or all return expressions are updating
    final int cl = cases.length;
    final Expr[] tmp = new Expr[cl];
    for(int c = 0; c < cl; c++) tmp[c] = cases[c].exprs[0];
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    for(final SwitchCase sc : cases) sc.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // check if expression can be pre-evaluated
    final Expr ex = opt(cc);
    if(ex != this) return optPre(ex, cc);

    // expression could not be pre-evaluated
    seqType = cases[0].exprs[0].seqType();
    final int cl = cases.length;
    for(int c = 1; c < cl; c++) seqType = seqType.union(cases[c].exprs[0].seqType());
    return ex;
  }

  /**
   * Optimizes the expression.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr opt(final CompileContext cc) throws QueryException {
    // pre-evaluate cases
    final boolean pre = cond.isValue();
    // cache expressions
    final ExprList cache = new ExprList();

    final Item it = pre ? cond.atomItem(cc.qc, info) : null;
    final ArrayList<SwitchCase> tmp = new ArrayList<>();
    for(final SwitchCase sc : cases) {
      final int sl = sc.exprs.length;
      final Expr ret = sc.exprs[0];
      final ExprList el = new ExprList(sl).add(ret);
      for(int e = 1; e < sl; e++) {
        final Expr ex = sc.exprs[e];
        if(pre && ex.isValue()) {
          // includes check for empty sequence (null reference)
          final Item cs = ex.item(cc.qc, info);
          if(it == cs || cs != null && it != null && it.equiv(cs, null, info)) return ret;
          cc.info(OPTREMOVE_X_X, description(), ex);
        } else if(cache.contains(ex)) {
          // case has already been checked before
          cc.info(OPTREMOVE_X_X, description(), ex);
        } else {
          cache.add(ex);
          el.add(ex);
        }
      }
      // return default branch (last one) if all others were discarded
      if(sl == 1 && tmp.isEmpty()) return ret;
      // build list of branches (add default branch and those that could not be pre-evaluated)
      if(sl == 1 || el.size() > 1) {
        sc.exprs = el.finish();
        tmp.add(sc);
      }
    }

    if(tmp.size() != cases.length) {
      // branches have changed
      cc.info(OPTREWRITE_X, this);
      cases = tmp.toArray(new SwitchCase[tmp.size()]);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(getCase(qc));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return qc.value(getCase(qc));
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return getCase(qc).item(qc, info);
  }

  @Override
  public boolean isVacuous() {
    for(final SwitchCase sc : cases) if(!sc.exprs[0].isVacuous()) return false;
    return true;
  }

  @Override
  public boolean has(final Flag flag) {
    for(final SwitchCase sc : cases) if(sc.has(flag)) return true;
    return cond.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    for(final SwitchCase sc : cases) if(!sc.removable(var)) return false;
    return cond.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    VarUsage max = VarUsage.NEVER, curr = VarUsage.NEVER;
    for(final SwitchCase cs : cases) {
      curr = curr.plus(cs.countCases(var));
      max = max.max(curr.plus(cs.count(var)));
    }
    return max.plus(cond.count(var));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean change = inlineAll(cases, var, ex, cc);
    final Expr cn = cond.inline(var, ex, cc);
    if(cn != null) {
      change = true;
      cond = cn;
    }
    return change ? optimize(cc) : null;
  }

  /**
   * Chooses the selected {@code case} expression.
   * @param qc query context
   * @return case expression
   * @throws QueryException query exception
   */
  private Expr getCase(final QueryContext qc) throws QueryException {
    final Item it = cond.atomItem(qc, info);
    for(final SwitchCase sc : cases) {
      final int sl = sc.exprs.length;
      for(int e = 1; e < sl; e++) {
        // includes check for empty sequence (null reference)
        final Item cs = sc.exprs[e].item(qc, info);
        if(it == cs || it != null && cs != null && it.equiv(cs, null, info))
          return sc.exprs[0];
      }
      if(sl == 1) return sc.exprs[0];
    }
    // will never be evaluated
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Switch(info, cond.copy(cc, vm), Arr.copyAll(cc, vm, cases));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, cases);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(SWITCH + PAREN1 + cond + PAREN2);
    for(final SwitchCase sc : cases) sb.append(sc);
    return sb.toString();
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final SwitchCase sc : cases) sc.markTailCalls(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && visitAll(visitor, cases);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : cases) sz += e.exprSize();
    return sz;
  }
}
