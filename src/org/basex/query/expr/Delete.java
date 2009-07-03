package org.basex.query.expr;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;

/**
 * Delete expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Delete extends Expr {
  /** Expression list. */
  private Expr expr;

  /**
   * Constructor.
   * @param r return expression
   */
  public Delete(final Expr r) {
    expr = r;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    return null;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(token(DELETE));
    ser.closeElement();
  }

  @Override
  public String toString() {
    return DELETE + NODES + expr;
  }
}
