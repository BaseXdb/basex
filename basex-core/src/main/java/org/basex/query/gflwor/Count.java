package org.basex.query.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.GFLWOR.Clause;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;


/**
 * GFLWOR {@code count} clause.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Count extends Clause {
  /** Count variable. */
  final Var var;

  /**
   * Constructor.
   * @param var variable
   * @param info input info
   */
  public Count(final Var var, final InputInfo info) {
    super(info, var);
    this.var = var;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Counter. */
      private long i = 1;
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(!sub.next(qc)) return false;
        qc.set(var, Int.get(i++), info);
        return true;
      }
    };
  }

  @Override
  boolean skippable(final Clause cl) {
    if(!super.skippable(cl)) return false;

    // the clause should not change tuple counts
    final long[] minMax = { 1, 1 };
    cl.calcSize(minMax);
    return minMax[0] == 1 && minMax[1] == 1;
  }

  @Override
  public boolean has(final Flag flag) {
    return false;
  }

  @Override
  public Count compile(final QueryContext qc, final VarScope scp) throws QueryException {
    return optimize(qc, scp);
  }

  @Override
  public Count optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    var.refineType(SeqType.ITR, qc, info);
    return this;
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.NEVER;
  }

  @Override
  public Clause inline(final QueryContext qc, final VarScope scp, final Var v, final Expr ex) {
    return null;
  }

  @Override
  public Count copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Var v = scp.newCopyOf(qc, var);
    vs.put(var.id, v);
    return new Count(v, info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.declared(var);
  }

  @Override
  public void checkUp() {
    // never
  }

  @Override
  void calcSize(final long[] minMax) {
  }

  @Override
  public int exprSize() {
    return 0;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    var.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    return "count " + var;
  }
}
