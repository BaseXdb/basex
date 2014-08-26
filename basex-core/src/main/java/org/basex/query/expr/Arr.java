package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract array expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Arr extends ParseExpr {
  /** Expressions. */
  public Expr[] exprs;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  protected Arr(final InputInfo info, final Expr... exprs) {
    super(info);
    this.exprs = exprs;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(exprs);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) exprs[e] = exprs[e].compile(qc, scp);
    return this;
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Expr e : exprs) if(e.has(flag)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Expr e : exprs) if(!e.removable(var)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, exprs);
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    return inlineAll(qc, scp, exprs, var, ex) ? optimize(qc, scp) : null;
  }

  /**
   * Creates a deep copy of the given array.
   * @param <T> element type
   * @param qc query context
   * @param scp variable scope
   * @param vs variable mapping
   * @param arr array to copy
   * @return deep copy of the array
   */
  @SuppressWarnings("unchecked")
  public static <T extends Expr> T[] copyAll(final QueryContext qc, final VarScope scp,
      final IntObjMap<Var> vs, final T[] arr) {
    final T[] copy = arr.clone();
    for(int i = 0; i < copy.length; i++) copy[i] = (T) copy[i].copy(qc, scp, vs);
    return copy;
  }

  /**
   * Returns true if all arguments are values.
   * @return result of check
   */
  protected final boolean allAreValues() {
    for(final Expr e : exprs) if(!e.isValue()) return false;
    return true;
  }

  /**
   * Returns true if at least one argument is empty or will yield 0 results.
   * @return result of check
   */
  protected final boolean oneIsEmpty() {
    for(final Expr e : exprs) if(e.isEmpty()) return true;
    return false;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), exprs);
  }

  /**
   * Prints the array with the specified separator.
   * @param sep separator
   * @return string representation
   */
  protected String toString(final String sep) {
    return new TokenBuilder(PAREN1).addSep(exprs, sep).add(PAREN2).toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, exprs);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : exprs) sz += e.exprSize();
    return sz;
  }
}
