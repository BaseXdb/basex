package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;

/**
 * Transform expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Transform extends Arr {
  /** Transform expressions. */
  private final ForLet[] fl;

  /**
   * Constructor.
   * @param f for / let expressions
   * @param e1 expression
   * @param e2 expression
   */
  public Transform(final ForLet[] f, final Expr e1, final Expr e2) {
    super(e1, e2);
    fl = f;
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(COPY + ' ');
    for(final ForLet t : fl) sb.append(t.var + ASSIGN + t.expr + ' ');
    return sb.append(MODIFY + ' ' + expr[0] + ' '  + RETURN + ' ' +
        expr[1]).toString();
  }
}
