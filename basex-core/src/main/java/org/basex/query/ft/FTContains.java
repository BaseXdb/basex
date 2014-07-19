package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Abstract FTContains expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class FTContains extends ParseExpr {
  /** Full-text parser. */
  final FTLexer lex;
  /** Expression. */
  Expr expr;
  /** Full-text expression. */
  FTExpr ftexpr;

  /**
   * Constructor.
   * @param expr expression
   * @param ftexpr full-text expression
   * @param info input info
   */
  protected FTContains(final Expr expr, final FTExpr ftexpr, final InputInfo info) {
    super(info);
    this.expr = expr;
    this.ftexpr = ftexpr;
    seqType = SeqType.BLN;
    lex = new FTLexer(new FTOpt());
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public final Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    expr = expr.compile(qc, scp);
    ftexpr = ftexpr.compile(qc, scp);
    return expr.isEmpty() ? optPre(Bln.FALSE, qc) : this;
  }

  @Override
  public final boolean has(final Flag flag) {
    return expr.has(flag) || ftexpr.has(flag);
  }

  @Override
  public final boolean removable(final Var var) {
    return expr.removable(var) && ftexpr.removable(var);
  }

  @Override
  public final VarUsage count(final Var var) {
    return expr.count(var).plus(ftexpr.count(var));
  }

  @Override
  public final Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {

    final Expr e = expr.inline(qc, scp, var, ex);
    if(e != null) expr = e;
    final FTExpr fte = ftexpr.inline(qc, scp, var, ex);
    if(fte != null) ftexpr = fte;
    return e != null || fte != null ? optimize(qc, scp) : null;
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, ftexpr);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && ftexpr.accept(visitor);
  }

  @Override
  public final int exprSize() {
    return expr.exprSize() + ftexpr.exprSize() + 1;
  }

  @Override
  public final String toString() {
    return expr + " " + CONTAINS + ' ' + TEXT + ' ' + ftexpr;
  }
}
