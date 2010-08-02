package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Abstract single expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
abstract class Single extends ParseExpr {
  /** Expression list. */
  public Expr expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  protected Single(final InputInfo ii, final Expr e) {
    super(ii);
    expr = e;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    return this;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return expr.uses(u, ctx);
  }

  @Override
  public Expr remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    ser.closeElement();
  }
}
