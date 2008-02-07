package org.basex.util;

import org.basex.query.xpath.expr.Expr;

/**
 * This is a simple container for expressions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
/** Expression Builder class. */
public final class ExprList {
  /** Expression array. */
  private Expr[] expr = new Expr[0];

  /**
   * Adds an expression.
   * @param e expression
   */
  public void add(final Expr e) {
    final int size = expr.length;
    final Expr[] tmp = new Expr[size + 1];
    System.arraycopy(expr, 0, tmp, 0, size);
    expr = tmp;
    expr[size] = e;
  }

  /**
   * Returns the expression array.
   * @return expressions
   */
  public Expr[] get() {
    return expr;
  }
}
