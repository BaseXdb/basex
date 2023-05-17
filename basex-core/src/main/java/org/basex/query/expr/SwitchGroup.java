package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Group of switch cases (case ... case ... return ...).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class SwitchGroup extends Arr {
  /** Deep equality comparison. */
  private final DeepEqual deep;

  /**
   * Constructor.
   * @param info input info
   * @param exprs return expression (placed first) and cases (default branch has 0 cases)
   */
  public SwitchGroup(final InputInfo info, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
    deep = new DeepEqual(info);
  }

  @Override
  public void checkUp() throws QueryException {
    final int el = exprs.length;
    for(int e = 1; e < el; ++e) checkNoUp(exprs[e]);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    // compile and simplify branches
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      exprs[e] = cc.compileOrError(exprs[e], false);
    }
    return optimize(cc);
  }

  @Override
  public SwitchGroup optimize(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      exprs[e] = exprs[e].simplifyFor(Simplify.STRING, cc);
    }
    return (SwitchGroup) adoptType(rtrn());
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new SwitchGroup(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    return ic.inline(exprs, true) ? optimize(ic.cc) : null;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    Expr ex = rtrn();
    try {
      ex = tc.check(ex, cc);
    } catch(final QueryException qe) {
      ex = cc.error(qe, ex);
    }
    // returned expression will be handled Switch#typeCheck
    if(ex == null) return null;
    exprs[0] = ex;
    return optimize(cc);
  }

  /**
   * {@inheritDoc}
   * This method counts only the occurrences in the return expression.
   */
  @Override
  public VarUsage count(final Var var) {
    return rtrn().count(var);
  }

  /**
   * Checks how often a variable is used in this expression.
   * This method counts only the occurrences in the case expressions.
   * @param var variable to look for
   * @return number of occurrences
   */
  VarUsage countCases(final Var var) {
    VarUsage uses = VarUsage.NEVER;
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      uses = uses.plus(exprs[e].count(var));
      if(uses == VarUsage.MORE_THAN_ONCE) break;
    }
    return uses;
  }

  /**
   * Checks if the switch group matches the supplied item.
   * @param cond condition
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  boolean match(final Item cond, final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      if(match(cond, e, qc)) return true;
    }
    return el == 1;
  }

  /**
   * Checks if the switch group matches the supplied item.
   * @param cond condition
   * @param e index of expression
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  boolean match(final Item cond, final int e, final QueryContext qc) throws QueryException {
    final Value value = exprs[e].atomValue(qc, info);
    if(cond.isEmpty()) return cond == value;
    for(final Item item : value) {
      if(deep.equal(cond, item)) return true;
    }
    return false;
  }

  /**
   * Returns the return expression.
   * @return return expression
   */
  Expr rtrn() {
    return exprs[0];
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // varying behavior: return null if return expression has changed
    final Expr rtrn = rtrn();
    exprs[0] = rtrn.simplifyFor(mode, cc);
    return rtrn != exprs[0] ? null : this;
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    rtrn().markTailCalls(cc);
  }

  @Override
  public int exprSize() {
    int size = 0;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SwitchGroup && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    final int el = exprs.length;
    for(int e = 1; e < el; e++) qs.token(CASE).token(exprs[e]);
    if(el == 1) qs.token(DEFAULT);
    qs.token(RETURN).token(rtrn());
  }
}
