package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;

/**
 * Transform Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class Transform extends Arr {
  /** Transform expressions. */
  private ForLet[] fl;

  /**
   * Constructor.
   * @param f for / let expressions
   * @param e1 expression
   * @param e2 expression
   */
  public Transform(final ForLet[] f, final Expr e1, final Expr e2) {
    fl = f;
    expr[0] = e1;
    expr[1] = e2;
  }

  @Override
  public String toString() {
    String s = "";
    for(ForLet t : fl) {
      s += t.var + ASSIGN + t.expr;
    }
    return COPY + s + MODIFY + expr[0] + RETURN + expr[1];
  }
}
