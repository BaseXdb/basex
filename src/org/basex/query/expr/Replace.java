package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;

/**
 * Replace expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class Replace extends Arr {
  /** 'Value of' flag. */
  private final boolean value;

  /**
   * Constructor.
   * @param t target expression
   * @param e replace expression
   * @param v replace value of
   */
  public Replace(final Expr t, final Expr e, final boolean v) {
    super(t, e);
    value = v;
  }

  @Override
  public String toString() {
    return REPLACE + (value ? VALUEE + OF : "") + NODE + expr[0] +
    WITH + expr[1];
  }
}
