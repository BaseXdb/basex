package org.basex.query.xpath.expr;

/**
 * Abstract Expression with two arguments.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class DualExpr extends Expr {
  /** First Expression. */
  public Expr expr1;
  /** Second Expression. */
  public Expr expr2;

  /**
   * Constructor.
   * @param e1 first expression (factor or dividend)
   * @param e2 second expression (factor or divisor)
   */
  public DualExpr(final Expr e1, final Expr e2) {
    expr1 = e1;
    expr2 = e2;
  }

  @Override
  public final boolean usesSize() {
    return expr1.usesSize() || expr2.usesSize();
  }

  @Override
  public final boolean usesPos() {
    return expr1.usesPos() || expr2.usesPos();
  }
}
