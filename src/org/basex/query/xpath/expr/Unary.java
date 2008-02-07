package org.basex.query.xpath.expr;

import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;

/**
 * XPath Unary Expression. The result of this expression is the change of the
 * sign of a number.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Unary extends Expr {
  /** Expression to be evaluated. */
  private Expr expr;

  /**
   * Constructor.
   * @param e expression evaluating to a node set.
   */
  public Unary(final Expr e) {
    expr = e;
  }

  @Override
  public Num eval(final XPContext ctx) throws QueryException {
    return new Num(-ctx.eval(expr).num());
  }

  @Override
  public boolean usesSize() {
    return expr.usesSize();
  }
  
  @Override
  public boolean usesPos() {
    return expr.usesPos();
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    expr = expr.compile(ctx);
    return expr instanceof Item ? new Num(-((Item) expr).num()) :
      this;
  }

  @Override
  public String toString() {
    return '-' + expr.toString();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    expr.plan(ser);
    ser.closeElement(this);
  }
}
