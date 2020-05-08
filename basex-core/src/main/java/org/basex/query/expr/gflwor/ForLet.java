package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * FLWOR {@code for}/{@code let} clause.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
abstract class ForLet extends Clause {
  /** Item variable. */
  public final Var var;
  /** Bound expression. */
  public Expr expr;
  /** Scoring flag. */
  boolean scoring;

  /**
   * Constructor.
   * @param info input info
   * @param seqType sequence type
   * @param var variables
   * @param expr expression
   * @param vars variable
   * @param scoring scoring flag
   */
  ForLet(final InputInfo info, final SeqType seqType, final Var var, final Expr expr,
      final boolean scoring, final Var... vars) {
    super(info, seqType, vars);
    this.var = var;
    this.expr = expr;
    this.scoring = scoring;
  }

  @Override
  public final Clause compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return optimize(cc);
  }

  @Override
  public final boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public final boolean inlineable(final Var v) {
    return expr.inlineable(v);
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
    return optimize(cc);
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
