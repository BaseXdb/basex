package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract base for switch and typeswitch expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <G> group type
 */
public abstract class ASwitch<G extends ParseExpr> extends ParseExpr {
  /** Condition. */
  Expr cond;
  /** Case groups (last one is default case). */
  G[] groups;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param cond condition
   * @param groups case groups (last one is default case)
   */
  ASwitch(final InputInfo info, final Expr cond, final G[] groups) {
    super(info, Types.ITEM_ZM);
    this.cond = cond;
    this.groups = groups;
  }

  /**
   * Returns the return expression of a group.
   * @param group group
   * @return return expression
   */
  abstract Expr branch(G group);

  /**
   * Assigns a new return expression to a group.
   * @param group group
   * @param expr return expression
   */
  abstract void branch(G group, Expr expr);

  /**
   * Returns the return expressions of all groups.
   * @return expressions
   */
  public final Expr[] branches() {
    final int gl = groups.length;
    final Expr[] branches = new Expr[gl];
    for(int g = 0; g < gl; g++) branches[g] = branch(groups[g]);
    return branches;
  }

  @Override
  public final boolean navigational() {
    return Checks.all(groups, group -> branch(group).navigational());
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    for(final G group : groups) group.compile(cc);
    return optimize(cc);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    boolean changed = false;
    for(final G group : groups) {
      changed |= group.simplifyFor(mode, cc) == null;
    }
    return changed ? optimize(cc) : super.simplifyFor(mode, cc);
  }

  @Override
  public final boolean vacuous() {
    return Checks.all(groups, group -> branch(group).vacuous());
  }

  @Override
  public final boolean ddo() {
    return Checks.all(groups, group -> branch(group).ddo());
  }

  @Override
  public final boolean has(final Flag... flags) {
    for(final G group : groups) {
      if(group.has(flags)) return true;
    }
    return cond.has(flags);
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    for(final G group : groups) {
      if(!group.inlineable(ic)) return false;
    }
    return cond.inlineable(ic);
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = ic.inline(groups, true);
    final Expr inlined = cond.inline(ic);
    if(inlined != null) {
      changed = true;
      cond = inlined;
    }
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public final Expr inlineTypeCheck(final TypeCheck tc, final CompileContext cc)
      throws QueryException {
    // ((type)switch(C) case X return A default return B) coerce to T →
    //   (type)switch(C) case X return (A coerce to T) default return (B coerce to T)
    boolean changed = false;
    for(final G group : groups) {
      changed |= group.inlineTypeCheck(tc, cc) != null;
    }
    return changed ? optimize(cc) : this;
  }

  @Override
  public final Expr mapBranches(final QueryFunction<Expr, Expr> func, final CompileContext cc)
      throws QueryException {
    // F((type)switch(C) case X return A default return B) →
    //   (type)switch(C) case X return F(A) default return F(B)
    for(final G group : groups) {
      branch(group, func.apply(branch(group)));
      group.optimize(cc);
    }
    return optimize(cc);
  }

  @Override
  public final void markTailCalls(final CompileContext cc) {
    for(final G group : groups) group.markTailCalls(cc);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && visitAll(visitor, groups);
  }

  @Override
  public final int exprSize() {
    int size = 1;
    for(final G group : groups) size += group.exprSize();
    return size + cond.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final ASwitch<?> s && cond.equals(s.cond) &&
        Arrays.equals(groups, s.groups);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), cond, groups);
  }
}
