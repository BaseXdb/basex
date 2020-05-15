package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Typeswitch expression.
 *
 * @author BaseX Team 2005-20, BSD License
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
    for(final TypeswitchGroup group : groups) group.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // pre-evaluate expression
    if(cond instanceof Value) {
      final Value value = (Value) cond;
      for(final TypeswitchGroup group : groups) {
        if(group.instance(value)) {
          group.inline(cc, value);
          return cc.replaceWith(this, group.expr);
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

    Expr expr = this;
    final int gl = groups.length;
    if(!cond.has(Flag.NDT)) {
      // check if always the same branch will be chosen (most specific branches occur first)
      TypeswitchGroup tg = null;
      for(final TypeswitchGroup group : groups) {
        if(tg == null && group.instance(ct)) tg = group;
      }

      // check if it's always the default branch that will be evaluated
      if(tg == null) {
        boolean opt = true;
        for(int g = 0; opt && g < gl - 1; g++) opt = groups[g].isNever(ct);
        if(opt) tg = groups[gl - 1];
      }

      // return first expression if all return expressions are equal
      if(tg == null) {
        boolean opt = true;
        for(int g = 1; opt && g < gl; g++) opt = groups[0].expr.equals(groups[g].expr);
        if(opt) tg = groups[0];
      }

      if(tg != null) {
        expr = tg.rewrite(cond, cc);
      } else if(gl < 3 && groups[0].seqTypes.length == 1) {
        // one or two branches: rewrite to if expression
        final Expr iff = new Instance(info, cond, groups[0].seqTypes[0]).optimize(cc);
        final Expr thn = groups[0].rewrite(cond, cc), els = groups[1].rewrite(cond, cc);
        expr = new If(info, iff, thn, els).optimize(cc);
      }
    }
    if(expr != this) return cc.replaceWith(this, expr);

    // combine types of return expressions
    SeqType st = groups[0].seqType();
    for(int g = 1; g < gl; g++) st = st.union(groups[g].seqType());
    exprType.assign(st);

    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    boolean changed = false;
    for(final TypeswitchGroup group : groups) {
      changed |= group.simplify(mode, cc);
    }
    return changed ? optimize(cc) : super.simplifyFor(mode, cc);
  }

  @Override
  public Data data() {
    final ExprList list = new ExprList(groups.length);
    for(final TypeswitchGroup group : groups) list.add(group);
    return data(list.finish());
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value seq = cond.value(qc);
    for(final TypeswitchGroup group : groups) {
      final Iter iter = group.iter(qc, seq);
      if(iter != null) return iter;
    }
    throw Util.notExpected();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value seq = cond.value(qc);
    for(final TypeswitchGroup group : groups) {
      final Value value = group.value(qc, seq);
      if(value != null) return value;
    }
    throw Util.notExpected();
  }

  @Override
  public boolean isVacuous() {
    for(final TypeswitchGroup group : groups) {
      if(!group.expr.isVacuous()) return false;
    }
    return true;
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final TypeswitchGroup group : groups) {
      if(group.has(flags)) return true;
    }
    return cond.has(flags);
  }

  @Override
  public boolean inlineable(final Var var) {
    for(final TypeswitchGroup group : groups) {
      if(!group.inlineable(var)) return false;
    }
    return cond.inlineable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return cond.count(var).plus(VarUsage.maximum(var, groups));
  }

  @Override
  public Expr inline(final ExprInfo ei, final Expr ex, final CompileContext cc)
      throws QueryException {
    boolean changed = inlineAll(ei, ex, groups, cc);
    final Expr inlined = cond.inline(ei, ex, cc);
    if(inlined != null) {
      changed = true;
      cond = inlined;
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final TypeswitchGroup group : groups) group.markTailCalls(cc);
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
        addSeparated(groups, " ").toString();
  }
}
