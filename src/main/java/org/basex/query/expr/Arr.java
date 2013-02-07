package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Abstract array expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Arr extends ParseExpr {
  /** Expression list. */
  public Expr[] expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  protected Arr(final InputInfo ii, final Expr... e) {
    super(ii);
    expr = e;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp();
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    for(int e = 0; e < expr.length; e++) expr[e] = expr[e].compile(ctx, scp);
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Expr e : expr) if(e.uses(u)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : expr) if(!e.removable(v)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].remove(v);
    return this;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.sum(v, expr);
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return inlineAll(ctx, scp, expr, v, e) ? optimize(ctx, scp) : null;
  }

  /**
   * Creates a deep copy of the given array.
   * @param <T> element type
   * @param ctx query context
   * @param scp variable scope
   * @param vs variable mapping
   * @param arr array to copy
   * @return deep copy of the array
   */
  @SuppressWarnings("unchecked")
  public static final <T extends Expr> T[] copyAll(final QueryContext ctx,
      final VarScope scp, final IntMap<Var> vs, final T[] arr) {
    final T[] copy = arr.clone();
    for(int i = 0; i < copy.length; i++) copy[i] = (T) copy[i].copy(ctx, scp, vs);
    return copy;
  }

  @Override
  public boolean databases(final StringList db) {
    for(final Expr e : expr) if(!e.databases(db)) return false;
    return true;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int e = 0; e < expr.length; ++e) expr[e] = expr[e].indexEquivalent(ic);
    return this;
  }

  /**
   * Returns true if all arguments are values.
   * @return result of check
   */
  protected final boolean allAreValues() {
    for(final Expr e : expr) if(!e.isValue()) return false;
    return true;
  }

  /**
   * Returns true if at least one argument is empty or will yield 0 results.
   * @return result of check
   */
  final boolean oneIsEmpty() {
    for(final Expr e : expr) if(e.isEmpty()) return true;
    return false;
  }

  /**
   * Checks if none of the expressions are updating expressions.
   * @throws QueryException query exception
   */
  public final void checkNoUp() throws QueryException {
    checkNoneUp(expr);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr);
  }

  /**
   * Prints the array with the specified separator.
   * @param sep separator
   * @return string representation
   */
  protected String toString(final String sep) {
    return new TokenBuilder(PAR1).addSep(expr, sep).add(PAR2).toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, expr);
  }
}
