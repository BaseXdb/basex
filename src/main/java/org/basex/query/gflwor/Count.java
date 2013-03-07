package org.basex.query.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.GFLWOR.Clause;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.util.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;


/**
 * GFLWOR {@code count} clause.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Count extends GFLWOR.Clause {
  /** Count variable. */
  final Var count;

  /**
   * Constructor.
   * @param v variable
   * @param ii input info
   */
  public Count(final Var v, final InputInfo ii) {
    super(ii, v);
    count = v;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Counter. */
      private long i = 1;
      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        if(!sub.next(ctx)) return false;
        ctx.set(count, Int.get(i++), info);
        return true;
      }
    };
  }

  @Override
  boolean skippable(final Clause cl) {
    // the clause should not change tuple counts
    return super.skippable(cl) && cl.calcSize(1) == 1;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    count.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    return "count " + count;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30;
  }

  @Override
  public Count compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    count.refineType(SeqType.ITR, ctx, info);
    return this;
  }

  @Override
  public Count optimize(final QueryContext ctx, final VarScope scp)
      throws QueryException {
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
  public Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return null;
  }

  @Override
  public Count copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final Var v = scp.newCopyOf(ctx, count);
    vs.add(count.id, v);
    return new Count(v, info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.declared(count);
  }

  @Override
  public void checkUp() throws QueryException {
    // never
  }

  @Override
  public boolean databases(final StringList db) {
    return true;
  }

  @Override
  long calcSize(final long cnt) {
    return cnt;
  }

  @Override
  public int exprSize() {
    return 0;
  }
}
