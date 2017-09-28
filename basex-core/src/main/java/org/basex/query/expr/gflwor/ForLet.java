package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * FLWOR {@code for}/{@code let} clause.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class ForLet extends Clause {
  /** Item variable. */
  public final Var var;
  /** Bound expression. */
  public Expr expr;
  /** Scoring flag. */
  final boolean scoring;

  /**
   * Constructor.
   * @param info input info
   * @param var variables
   * @param expr expression
   * @param vars variable
   * @param scoring scoring flag
   */
  ForLet(final InputInfo info, final Var var, final Expr expr, final boolean scoring,
      final Var... vars) {
    super(info, vars);
    this.var = var;
    this.expr = expr;
    this.scoring = scoring;
  }

  @Override
  public Clause compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return optimize(cc);
  }

  @Override
  public final boolean has(final Flag flag) {
    return expr.has(flag);
  }

  @Override
  public final boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public final VarUsage count(final Var v) {
    return expr.count(v);
  }

  @Override
  public final Clause inline(final Var v, final Expr ex, final CompileContext cc)
      throws QueryException {

    final Expr sub = expr.inline(v, ex, cc);
    if(sub == null) return null;
    expr = sub;
    // call compile instead of optimize, because many new optimizations may be triggered by inlining
    return compile(cc);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public final int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof ForLet)) return false;
    final ForLet fl = (ForLet) obj;
    return expr.equals(fl.expr) && var.equals(fl.var) && scoring == fl.scoring;
  }
}
