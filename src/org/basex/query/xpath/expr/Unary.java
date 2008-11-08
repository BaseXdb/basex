package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Dbl;

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
  public Expr comp(final XPContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    return expr instanceof Item ? new Dbl(-((Item) expr).num()) : this;
  }

  @Override
  public Dbl eval(final XPContext ctx) throws QueryException {
    return new Dbl(-ctx.eval(expr).num());
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
  public String toString() {
    return "-" + expr;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    ser.closeElement();
  }
}
