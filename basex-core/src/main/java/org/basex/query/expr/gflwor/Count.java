package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
import org.basex.query.expr.gflwor.GFLWOR.Eval;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * GFLWOR {@code count} clause.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class Count extends Clause {
  /** Count variable. */
  final Var var;

  /**
   * Constructor.
   * @param var variable
   */
  public Count(final Var var) {
    super(var.info, var);
    this.var = var;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      private long i = 1;

      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(!sub.next(qc)) return false;
        qc.set(var, Int.get(i++));
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
  public Count compile(final CompileContext cc) throws QueryException {
    return optimize(cc);
  }

  @Override
  public Count optimize(final CompileContext cc) throws QueryException {
    var.refineType(SeqType.ITR, cc);
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
  public Clause inline(final Var v, final Expr ex, final CompileContext cc) {
    return null;
  }

  @Override
  public Count copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Count(cc.copy(var, vm));
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
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Count && var.equals(((Count) obj).var);
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
