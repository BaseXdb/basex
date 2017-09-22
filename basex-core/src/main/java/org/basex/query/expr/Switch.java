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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Switch extends ParseExpr {
  /** Case groups. */
  private SwitchGroups[] groups;
  /** Condition. */
  private Expr cond;

  /**
   * Constructor.
   * @param info input info
   * @param cond condition
   * @param groups case groups (last one is default case)
   */
  public Switch(final InputInfo info, final Expr cond, final SwitchGroups[] groups) {
    super(info);
    this.groups = groups;
    this.cond = cond;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    for(final SwitchGroups group : groups) group.checkUp();
    // check if none or all return expressions are updating
    final int cl = groups.length;
    final Expr[] tmp = new Expr[cl];
    for(int c = 0; c < cl; c++) tmp[c] = groups[c].exprs[0];
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    for(final SwitchGroups group : groups) group.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // check if expression can be pre-evaluated
    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // expression could not be pre-evaluated
    seqType = groups[0].exprs[0].seqType();
    final int cl = groups.length;
    for(int c = 1; c < cl; c++) seqType = seqType.union(groups[c].exprs[0].seqType());
    return this;
  }

  /**
   * Optimizes the expression.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr opt(final CompileContext cc) throws QueryException {
    // cached switch cases
    final ExprList cases = new ExprList();
    final Item it = cond.isValue() ? cond.atomItem(cc.qc, info) : null;
    final ArrayList<SwitchGroups> tmpGroups = new ArrayList<>();
    for(final SwitchGroups group : groups) {
      final int el = group.exprs.length;
      final Expr ret = group.exprs[0];
      final ExprList list = new ExprList(el).add(ret);
      for(int e = 1; e < el; e++) {
        final Expr ex = group.exprs[e];
        if(cond.isValue() && ex.isValue()) {
          // includes check for empty sequence (null reference)
          final Item cs = ex.atomItem(cc.qc, info);
          if(it == cs || cs != null && it != null && it.equiv(cs, null, info)) return ret;
          cc.info(OPTREMOVE_X_X, description(), ex);
        } else if(cases.contains(ex)) {
          // case has already been checked before
          cc.info(OPTREMOVE_X_X, description(), ex);
        } else {
          cases.add(ex);
          list.add(ex);
        }
      }
      // build list of branches (add those with case left, or the default branch)
      if(list.size() > 1 || el == 1) {
        group.exprs = list.finish();
        tmpGroups.add(group);
      }
    }

    if(tmpGroups.size() != groups.length) {
      // branches have changed
      cc.info(OPTSIMPLE_X, this);
      groups = tmpGroups.toArray(new SwitchGroups[tmpGroups.size()]);
    }

    // return default branch (last one) if all others were discarded
    if(groups.length == 1) return groups[0].exprs[0];

    if(groups.length == 2 && groups[0].exprs.length == 2) {
      //return new If(info, new CmpV(cond, groups[0].exprs[1], null, info)));
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
    for(final SwitchGroups group : groups) {
      if(!group.exprs[0].isVacuous()) return false;
    }
    return true;
  }

  @Override
  public boolean has(final Flag flag) {
    for(final SwitchGroups group : groups) {
      if(group.has(flag)) return true;
    }
    return cond.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    for(final SwitchGroups group : groups) {
      if(!group.removable(var)) return false;
    }
    return cond.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    VarUsage max = VarUsage.NEVER, curr = VarUsage.NEVER;
    for(final SwitchGroups cs : groups) {
      curr = curr.plus(cs.countCases(var));
      max = max.max(curr.plus(cs.count(var)));
    }
    return max.plus(cond.count(var));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean change = inlineAll(groups, var, ex, cc);
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
    for(final SwitchGroups group : groups) {
      final int gl = group.exprs.length;
      for(int e = 1; e < gl; e++) {
        // includes check for empty sequence (null reference)
        final Item cs = group.exprs[e].atomItem(qc, info);
        if(it == cs || it != null && cs != null && it.equiv(cs, null, info))
          return group.exprs[0];
      }
      if(gl == 1) return group.exprs[0];
    }
    // will never be evaluated
    throw Util.notExpected();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Switch(info, cond.copy(cc, vm), Arr.copyAll(cc, vm, groups));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, groups);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(SWITCH + PAREN1 + cond + PAREN2);
    for(final SwitchGroups group : groups) sb.append(group);
    return sb.toString();
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final SwitchGroups group : groups) group.markTailCalls(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && visitAll(visitor, groups);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : groups) sz += e.exprSize();
    return sz;
  }
}
