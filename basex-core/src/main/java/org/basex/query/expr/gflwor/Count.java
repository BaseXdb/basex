package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * GFLWOR {@code count} clause.
 *
 * @author BaseX Team, BSD License
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
    super(var.info, SeqType.INTEGER_O, var);
    this.var = var;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      private long i = 1;

      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(!sub.next(qc)) return false;
        qc.set(var, Itr.get(i++));
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
  public boolean has(final Flag... flags) {
    return false;
  }

  @Override
  public Count compile(final CompileContext cc) throws QueryException {
    return optimize(cc);
  }

  @Override
  public Count optimize(final CompileContext cc) throws QueryException {
    var.refineType(seqType(), cc);
    return this;
  }

  @Override
  public boolean inlineable(final InlineContext v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.NEVER;
  }

  @Override
  public Clause inline(final InlineContext ic) {
    return null;
  }

  @Override
  public Count copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Count(cc.copy(var, vm)));
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
  public int exprSize() {
    return 0;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Count count && var.equals(count.var);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.attachVariable(plan.create(this), var, false));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(COUNT).token(var);
  }
}
