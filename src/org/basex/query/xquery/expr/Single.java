package org.basex.query.xquery.expr;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Type;

/**
 * Abstract single expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Single extends Expr {
  /** Expression list. */
  public Expr expr;

  /**
   * Constructor.
   * @param e expression
   */
  protected Single(final Expr e) {
    expr = e;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    expr = expr.comp(ctx);
    return this;
  }

  @Override
  public boolean uses(final Using u) {
    return expr.uses(u);
  }

  @Override
  public Type returned() {
    return null;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    expr.plan(ser);
    ser.closeElement(this);
  }
}
