package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Typeswitch expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Typeswitch extends ParseExpr {
  /** Cases. */
  private TypeswitchGroup[] groups;
  /** Condition. */
  private Expr cond;

  /**
   * Constructor.
   * @param info input info
   * @param cond condition
   * @param groups case expressions
   */
  public Typeswitch(final InputInfo info, final Expr cond, final TypeswitchGroup[] groups) {
    super(info, SeqType.ITEM_ZM);
    this.cond = cond;
    this.groups = groups;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    final int gl = groups.length;
    final Expr[] tmp = new Expr[gl];
    for(int g = 0; g < gl; ++g) tmp[g] = groups[g].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    for(final TypeswitchGroup tg : groups) tg.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // pre-evaluate expression
    if(cond instanceof Value) {
      final Value value = (Value) cond;
      for(final TypeswitchGroup tg : groups) {
        if(tg.matches(value)) {
          tg.inline(cc, value);
          return cc.replaceWith(this, tg.expr);
        }
      }
    }

    // remove checks that will never match
    final SeqType ct = cond.seqType();
    final ArrayList<SeqType> types = new ArrayList<>();
    final ArrayList<TypeswitchGroup> newGroups = new ArrayList<>(groups.length);
    for(final TypeswitchGroup group : groups) {
      if(group.removeTypes(ct, types, cc)) newGroups.add(group);
    }
    groups = newGroups.toArray(new TypeswitchGroup[0]);

    final int gl = groups.length;
    if(!cond.has(Flag.NDT)) {
      // check if it's always the default branch that will be evaluated
      TypeswitchGroup tg = null;
      boolean opt = true;
      for(int g = 0; opt && g < gl - 1; g++) opt = !groups[g].canMatch(ct);
      if(opt) tg = groups[gl - 1];

      // return first expression if all return expressions are equal
      if(tg == null) {
        opt = true;
        for(int g = 1; opt && g < gl; g++) opt = groups[0].expr.equals(groups[g].expr);
        if(opt) tg = groups[0];
      }

      if(tg != null) {
        final Expr expr;
        if(tg.var != null) {
          final IntObjMap<Var> vm = new IntObjMap<>();
          final LinkedList<Clause> clauses = new LinkedList<>();
          clauses.add(new Let(cc.copy(tg.var, vm), cond, false).optimize(cc));
          final Expr rtrn = tg.expr.copy(cc, vm).optimize(cc);
          expr = new GFLWOR(info, clauses, rtrn).optimize(cc);
        } else {
          expr = tg.expr;
        }
        return cc.replaceWith(this, expr);
      }
    }

    // combine types of return expressions
    SeqType st = groups[0].seqType();
    for(int g = 1; g < gl; g++) st = st.union(groups[g].seqType());
    exprType.assign(st);
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value seq = cond.value(qc);
    for(final TypeswitchGroup tg : groups) {
      final Iter iter = tg.iter(qc, seq);
      if(iter != null) return iter;
    }
    throw Util.notExpected();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value seq = cond.value(qc);
    for(final TypeswitchGroup tg : groups) {
      final Value value = tg.value(qc, seq);
      if(value != null) return value;
    }
    throw Util.notExpected();
  }

  @Override
  public boolean isVacuous() {
    for(final TypeswitchGroup tg : groups) {
      if(!tg.expr.isVacuous()) return false;
    }
    return true;
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final TypeswitchGroup tg : groups) {
      if(tg.has(flags)) return true;
    }
    return cond.has(flags);
  }

  @Override
  public boolean inlineable(final Var var) {
    for(final TypeswitchGroup tg : groups) {
      if(!tg.inlineable(var)) return false;
    }
    return cond.inlineable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return cond.count(var).plus(VarUsage.maximum(var, groups));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean changed = inlineAll(var, ex, groups, cc);
    final Expr c = cond.inline(var, ex, cc);
    if(c != null) {
      changed = true;
      cond = c;
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final TypeswitchGroup tg : groups) tg.markTailCalls(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && visitAll(visitor, groups);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr group : groups) size += group.exprSize();
    return size + cond.exprSize();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Typeswitch(info, cond.copy(cc, vm), Arr.copyAll(cc, vm, groups)));
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Typeswitch)) return false;
    final Typeswitch ts = (Typeswitch) obj;
    return cond.equals(ts.cond) && Array.equals(groups, ts.groups);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), groups, cond);
  }

  @Override
  public String toString() {
    return new TokenBuilder().add(TYPESWITCH).add(PAREN1).add(cond).add(PAREN2).add(' ').
        addSep(groups, " ").toString();
  }
}
