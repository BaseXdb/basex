package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Switch expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Switch extends ASwitch<SwitchGroup> {
  /** Indexes of the groups of string cases (can be {@code null}). */
  private TokenIntMap strings;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param cond condition
   * @param groups case groups (last one is default case)
   */
  public Switch(final InputInfo info, final Expr cond, final SwitchGroup[] groups) {
    super(info, cond, groups);
  }

  @Override
  Expr branch(final SwitchGroup group) {
    return group.rtrn();
  }

  @Override
  void branch(final SwitchGroup group, final Expr expr) {
    group.exprs[0] = expr;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    for(final SwitchGroup group : groups) group.checkUp();
    // check if none or all return expressions are updating
    checkAllUp(branches());
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    cond = cond.simplifyFor(Simplify.STRING, cc);

    // check if expression can be pre-evaluated
    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // combine types of return expressions
    exprType.assign(SeqType.union(groups, true)).data(groups);

    strings = index();
    return this;
  }

  /**
   * Creates an index for the groups if all cases are strings and no collation is assigned.
   * @return map from strings to group indexes, or {@code null}
   */
  private TokenIntMap index() {
    if(info == null || sc().collation != null) return null;
    int size = 0;
    for(final SwitchGroup group : groups) {
      final Expr[] exprs = group.exprs;
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        if(!(exprs[e] instanceof final Str str) || str.type != BasicType.STRING) return null;
      }
      size += el - 1;
    }
    if(size < 4) return null;

    // add cases in reverse order: the first group with a given string wins
    final TokenIntMap index = new TokenIntMap(size);
    for(int g = groups.length - 1; g >= 0; g--) {
      final Expr[] exprs = groups[g].exprs;
      for(int e = exprs.length - 1; e >= 1; e--) index.put(((Str) exprs[e]).string(), g);
    }
    return index;
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
      final ExprList list = new ExprList(el).add(group.rtrn());
      for(int e = 1; e < el; e++) {
        final Expr expr = group.exprs[e];
        // check if same case expression exists more than once
        // (nondeterministic duplicates are kept, as they would otherwise lose their side effects)
        // case 'a' return A case 'a' return B → case 'a' return A
        boolean remove = cases.contains(expr) && !expr.has(Flag.NDT);
        if(!remove && cnd != null) {
          // pre-evaluate values; skip remaining checks if match is found
          if(expr instanceof Value) {
            // switch('a') case 'a' return A default return B → A
            if(group.match(cnd, e, cc.qc)) return group.rtrn();
            remove = true;
          } else {
            // value unknown at compile: perform no further compile-time checks
            cnd = null;
          }
        }
        if(remove) {
          cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
        } else {
          cases.add(expr);
          list.add(expr);
        }
      }
      // build list of branches (add those with case left, or the default branch)
      if(list.size() > 1 || el == 1) {
        group.exprs = list.finish();
        tmpGroups.add(group);
      }
    }

    // merge branches with identical return path
    for(int g = 1; g < tmpGroups.size(); g++) {
      final SwitchGroup group1 = tmpGroups.get(g - 1), group2 = tmpGroups.get(g);
      if(group1.rtrn().equals(group2.rtrn())) {
        if(g + 1 == tmpGroups.size() && !group1.has(Flag.NDT)) {
          // case 'a' return A default return A → default return A
          tmpGroups.set(g - 1, group2);
        } else {
          // case 'a' return A case 'b' return A → case 'a' case 'b' return A
          final ExprList list = new ExprList(group1.exprs.length + group2.exprs.length - 1);
          list.add(group1.exprs).add(Arrays.copyOfRange(group2.exprs, 1, group2.exprs.length));
          tmpGroups.set(g - 1, new SwitchGroup(group1.info, list.finish()).optimize(cc));
        }
        tmpGroups.remove(g--);
      }
    }

    // update branches
    if(tmpGroups.size() != groups.length) {
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
      groups = tmpGroups.toArray(SwitchGroup[]::new);
    }

    Expr expr = toBranch();
    if(expr == this) expr = toIf(cc);
    return expr;
  }

  /**
   * Rewrites a switch with identical branches to a single branch.
   * @return new or original expression
   */
  private Expr toBranch() {
    // only the default branch may be left at this stage: switch($x) default return A → A
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
    final boolean string = st.type.isStringOrUntyped(), dec = st.type.instanceOf(BasicType.DECIMAL);
    if(!st.one() || !(string || dec)) return this;

    final Expr[] exprs = groups[0].exprs;
    for(int e = exprs.length - 1; e >= 1; e--) {
      final SeqType est = exprs[e].seqType();
      if(!est.oneOrMore() || !(
        string && est.type.isStringOrUntyped() ||
        dec && est.type.instanceOf(BasicType.DECIMAL)
      )) return this;
    }

    // switch($c) case 'a' return B default return C → if($c = 'a') then B else C
    final Expr list = List.get(cc, groups[0].info, Arrays.copyOfRange(exprs, 1, exprs.length));
    final CmpG cmp = new CmpG(groups[0].info, cond, list, CmpOp.EQ);
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
  protected boolean ebv(final QueryContext qc) throws QueryException {
    return expr(qc).ebv(qc, info);
  }

  @Override
  public boolean eager() {
    return Checks.all(groups, group -> group.rtrn().eager());
  }

  /**
   * Tests the conditions and returns the expression to evaluate.
   * @param qc query context
   * @return case expression
   * @throws QueryException query exception
   */
  private Expr expr(final QueryContext qc) throws QueryException {
    final Item item = cond.atomItem(qc, info);
    if(strings != null && item.type.isStringOrUntyped()) {
      final int g = strings.get(item.string(info));
      return groups[g != Integer.MIN_VALUE ? g : groups.length - 1].rtrn();
    }
    for(final SwitchGroup group : groups) {
      if(group.match(item, qc)) return group.rtrn();
    }
    throw Util.notExpected();
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
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Switch(info, cond.copy(cc, vm), Arr.copyAll(cc, vm, groups)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Switch && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.open().token(SWITCH).paren(cond).tokens(groups).close();
  }
}
