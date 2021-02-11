package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Switch expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final ExprList rtrns = new ExprList(groups.length);
    for(final SwitchGroup group : groups) rtrns.add(group.rtrn());
    checkAllUp(rtrns.finish());
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    for(final SwitchGroup group : groups) group.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    cond = cond.simplifyFor(Simplify.STRING, cc);

    // check if expression can be pre-evaluated
    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // combine types of return expressions
    final int gl = groups.length;
    SeqType st = groups[0].seqType();
    for(int g = 1; g < gl; g++) st = st.union(groups[g].seqType());
    exprType.assign(st);

    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    boolean changed = false;
    for(final SwitchGroup group : groups) {
      changed |= group.simplify(mode, cc);
    }
    return changed ? optimize(cc) : super.simplifyFor(mode, cc);
  }

  /**
   * Optimizes the expression.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr opt(final CompileContext cc) throws QueryException {
    final ExprList cases = new ExprList();
    Item cnd = cond instanceof Value ? cond.atomItem(cc.qc, info) : null;
    final ArrayList<SwitchGroup> tmpGroups = new ArrayList<>();
    for(final SwitchGroup group : groups) {
      final int el = group.exprs.length;
      final Expr rtrn = group.rtrn();
      final ExprList list = new ExprList(el).add(rtrn);
      for(int e = 1; e < el; e++) {
        final Expr expr = group.exprs[e];
        if(cases.contains(expr)) {
          // case has already been checked before
          cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
          continue;
        }
        if(cnd != null) {
          // compare condition and value; return result or remove case
          if(expr instanceof Value) {
            final Item cs = expr.atomItem(cc.qc, info);
            if(cnd == cs || cs != Empty.VALUE && cnd != Empty.VALUE && cnd.equiv(cs, null, info)) {
              return rtrn;
            }
            cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
            continue;
          }
          // value unknown at compile: perform no further compile-time checks
          cnd = null;
        }
        cases.add(expr);
        list.add(expr);
      }
      // build list of branches (add those with case left, or the default branch)
      if(list.size() > 1 || el == 1) {
        group.exprs = list.finish();
        tmpGroups.add(group);
      }
    }

    // merge branches with identical return path
    for(int g = 0; g < tmpGroups.size(); g++) {
      final SwitchGroup group1 = g > 0 ? tmpGroups.get(g - 1) : null, group2 = tmpGroups.get(g);
      if(g > 0 && group1.rtrn().equals(group2.rtrn())) {
        if(g + 1 == tmpGroups.size() && !group1.has(Flag.NDT)) {
          tmpGroups.set(g - 1, group2);
        } else {
          final ExprList list = new ExprList(group1.exprs.length + group2.exprs.length - 1);
          list.add(group1.exprs).add(Arrays.copyOfRange(group2.exprs, 1, group2.exprs.length));
          tmpGroups.set(g - 1, new SwitchGroup(group1.info, list.finish()).optimize(cc));
        }
        tmpGroups.remove(g--);
      }
    }

    // update branches
    if(tmpGroups.size() != groups.length) {
      groups = tmpGroups.toArray(new SwitchGroup[0]);
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
    }

    Expr expr = simplify();
    if(expr == this) expr = toIf(cc);
    return expr;
  }

  /**
   * Simplifies a switch expression with identical branches.
   * @return new or original expression
   */
  private Expr simplify() {
    // only the default branch may be left at this stage
    final Expr expr = groups[0].rtrn();
    for(int g = groups.length - 1; g >= 1; g--) {
      if(!expr.equals(groups[g].rtrn())) return this;
    }
    return expr;
  }

  /**
   * Rewrites the switch to an if expression.
   * @param cc compilation context
   * @return new or original expression
   * @throws QueryException query exception
   */
  private Expr toIf(final CompileContext cc) throws QueryException {
    if(groups.length != 2) return this;

    final SeqType st = cond.seqType();
    final boolean string = st.type.isStringOrUntyped(), dec = st.type.instanceOf(AtomType.DECIMAL);
    if(!st.one() || !(string || dec)) return this;

    final Expr[] exprs = groups[0].exprs;
    for(int e = exprs.length - 1; e >= 1; e--) {
      final SeqType mt = exprs[e].seqType();
      if(!mt.one() || !(
        string && mt.type.isStringOrUntyped() ||
        dec && mt.type.instanceOf(AtomType.DECIMAL)
      )) return this;
    }

    final Expr list = List.get(cc, groups[0].info, Arrays.copyOfRange(exprs, 1, exprs.length));
    final CmpG cmp = new CmpG(cond, list, OpG.EQ, null, null, groups[0].info);
    return new If(info, cmp.optimize(cc), groups[0].rtrn(), groups[1].rtrn()).optimize(cc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return expr(qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return expr(qc).value(qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return expr(qc).item(qc, info);
  }

  /**
   * Tests the conditions and returns the expression to evaluate.
   * @param qc query context
   * @return case expression
   * @throws QueryException query exception
   */
  private Expr expr(final QueryContext qc) throws QueryException {
    final Item item = cond.atomItem(qc, info);
    for(final SwitchGroup group : groups) {
      if(group.match(item, qc)) return group.rtrn();
    }
    throw Util.notExpected();
  }

  @Override
  public boolean vacuous() {
    return ((Checks<SwitchGroup>) group -> group.rtrn().vacuous()).all(groups);
  }

  @Override
  public boolean ddo() {
    return ((Checks<SwitchGroup>) group -> group.rtrn().ddo()).all(groups);
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final SwitchGroup group : groups) {
      if(group.has(flags)) return true;
    }
    return cond.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final SwitchGroup group : groups) {
      if(!group.inlineable(ic)) return false;
    }
    return cond.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    VarUsage max = VarUsage.NEVER, uses = VarUsage.NEVER;
    for(final SwitchGroup cs : groups) {
      uses = uses.plus(cs.countCases(var));
      max = max.max(uses.plus(cs.count(var)));
    }
    return max.plus(cond.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = ic.inline(groups, true);
    final Expr inlined = cond.inline(ic);
    if(inlined != null) {
      changed = true;
      cond = inlined;
    }
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    boolean changed = false;
    for(final SwitchGroup group : groups) {
      changed = group.typeCheck(tc, cc) != null;
    }
    return changed ? optimize(cc) : this;
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
  public Data data() {
    final ExprList list = new ExprList(groups.length);
    for(final SwitchGroup group : groups) list.add(group.rtrn());
    return data(list.finish());
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && visitAll(visitor, groups);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr group : groups) size += group.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Switch)) return false;
    final Switch s = (Switch) obj;
    return cond.equals(s.cond) && Array.equals(groups, s.groups);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), cond, groups);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(SWITCH).paren(cond).tokens(groups);
  }
}
