package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Pragma extension.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Extension extends Single {
  /** Pragmas of the ExtensionExpression. */
  private final Expr[] pragmas;

  /**
   * Constructor.
   * @param ii input info
   * @param prag pragmas
   * @param e enclosed expression
   */
  public Extension(final InputInfo ii, final Expr[] prag, final Expr e) {
    super(ii, e);
    pragmas = prag;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // ignore pragma
    return optPre(expr.comp(ctx), ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // currently, will never be called, as compilation step returns argument
    return ctx.iter(expr);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr p : pragmas) p.plan(ser);
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr p : pragmas) sb.append(p).append(' ');
    return sb.append(BRACE1 + ' ' + expr + ' ' + BRACE2).toString();
  }

  @Override
  public Expr markTailCalls() {
    expr = expr.markTailCalls();
    return this;
  }
}
