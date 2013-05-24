package org.basex.query.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines is an abstract class for full-text expressions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class FTExpr extends ParseExpr {
  /** Expression list. */
  public final FTExpr[] expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  FTExpr(final InputInfo ii, final FTExpr... e) {
    super(ii);
    expr = e;
    type = SeqType.BLN;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(expr);
  }

  @Override
  public FTExpr compile(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    final int es = expr.length;
    for(int e = 0; e < es; e++) expr[e] = expr[e].compile(ctx, scp);
    return this;
  }

  @Override
  public FTExpr optimize(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    return this;
  }

  /**
   * This method is called by the sequential full-text evaluation.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * This method is called by the index-based full-text evaluation.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTIter iter(final QueryContext ctx) throws QueryException;

  @Override
  public boolean uses(final Use u) {
    for(final FTExpr e : expr) if(e.uses(u)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : expr) if(!e.removable(v)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.sum(v, expr);
  }

  @Override
  public FTExpr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return inlineAll(ctx, scp, expr, v, e) ? optimize(ctx, scp) : null;
  }

  @Override
  public abstract FTExpr copy(QueryContext ctx, VarScope scp, IntMap<Var> vs);

  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    final int es = expr.length;
    for(int e = 0; e < es; e++) expr[e] = expr[e].indexEquivalent(ic);
    return this;
  }

  /**
   * Checks if sub expressions of a mild not operator violate the grammar.
   * @return result of check
   */
  boolean usesExclude() {
    for(final FTExpr e : expr) if(e.usesExclude()) return true;
    return false;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, expr);
  }

  /**
   * Prints the array with the specified separator.
   * @param sep separator
   * @return string representation
   */
  final String toString(final Object sep) {
    final StringBuilder sb = new StringBuilder();
    final int es = expr.length;
    for(int e = 0; e < es; e++) sb.append(e != 0 ? sep.toString() : "").append(expr[e]);
    return sb.toString();
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : expr) sz += e.exprSize();
    return sz;
  }
}
