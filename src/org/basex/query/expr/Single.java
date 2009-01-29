package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Var;

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
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    return this;
  }

  @Override
  public boolean usesPos(final QueryContext ctx) {
    return expr.usesPos(ctx);
  }

  @Override
  public int countVar(final Var v) {
    return expr.countVar(v);
  }

  @Override
  public Expr removeVar(final Var v) {
    expr = expr.removeVar(v);
    return this;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return expr.returned(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    ser.closeElement();
  }
}
