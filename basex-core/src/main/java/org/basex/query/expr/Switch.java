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
import org.basex.query.value.type.*;
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
  /** Condition. */
  private Expr cond;
  /** Case groups. */
  private SwitchGroup[] groups;

  /**
   * Constructor.
   * @param info input info
   * @param cond condition
   * @param groups case groups (last one is default case)
   */
  public Switch(final InputInfo info, final Expr cond, final SwitchGroup[] groups) {
    super(info, SeqType.ITEM_ZM);
    this.cond = cond;
    this.groups = groups;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    for(final SwitchGroup group : groups) group.checkUp();
    // check if none or all return expressions are updating
    final int gl = groups.length;
    final Expr[] tmp = new Expr[gl];
    for(int g = 0; g < gl; g++) tmp[g] = groups[g].exprs[0];
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    for(final SwitchGroup group : groups) group.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // check if expression can be pre-evaluated
    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // combine types of return expressions
    SeqType st = groups[0].exprs[0].seqType();
    final int gl = groups.length;
    for(int g = 1; g < gl; g++) st = st.union(groups[g].exprs[0].seqType());
    exprType.assign(st);
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
    final Item it = cond instanceof Value ? cond.atomItem(cc.qc, info) : null;
    final ArrayList<SwitchGroup> tmpGroups = new ArrayList<>();
    for(final SwitchGroup group : groups) {
      final int el = group.exprs.length;
      final Expr ret = group.exprs[0];
      final ExprList list = new ExprList(el).add(ret);
      for(int e = 1; e < el; e++) {
        final Expr ex = group.exprs[e];
        if(cond instanceof Value && ex instanceof Value) {
          // includes check for empty sequence (null reference)
          final Item cs = ex.atomItem(cc.qc, info);
          if(it == cs || cs != null && it != null && it.equiv(cs, null, info)) return ret;
          cc.info(OPTREMOVE_X_X, ex, description());
        } else if(cases.contains(ex)) {
          // case has already been checked before
          cc.info(OPTREMOVE_X_X, ex, description());
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
      groups = tmpGroups.toArray(new SwitchGroup[tmpGroups.size()]);
    }

    // return first expression if all return expressions are equal, or if only one branch is left
    final Expr expr = groups[0].exprs[0];
    final int gl = groups.length;
    for(int g = 1; g < gl; g++) {
      if(!expr.equals(groups[g].exprs[0])) return this;
    }
    return expr;
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
    for(final SwitchGroup group : groups) {
      if(!group.exprs[0].isVacuous()) return false;
    }
    return true;
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final SwitchGroup group : groups) {
      if(group.has(flags)) return true;
    }
    return cond.has(flags);
  }

  @Override
  public boolean removable(final Var var) {
    for(final SwitchGroup group : groups) {
      if(!group.removable(var)) return false;
    }
    return cond.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    VarUsage max = VarUsage.NEVER, curr = VarUsage.NEVER;
    for(final SwitchGroup cs : groups) {
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
    for(final SwitchGroup group : groups) {
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
    return copyType(new Switch(info, cond.copy(cc, vm), Arr.copyAll(cc, vm, groups)));
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final SwitchGroup group : groups) group.markTailCalls(cc);
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

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Switch)) return false;
    final Switch s = (Switch) obj;
    return cond.equals(s.cond) && Array.equals(groups, s.groups);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, groups);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(SWITCH + PAREN1 + cond + PAREN2);
    for(final SwitchGroup group : groups) sb.append(group);
    return sb.toString();
  }
}
