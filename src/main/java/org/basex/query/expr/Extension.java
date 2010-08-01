package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.iter.Iter;

/**
 * Pragma extension.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leo Woerteler
 */
public final class Extension extends Single {
  /** Pragmas of the ExtensionExpression. */
  private final Expr[] pragmas;

  /**
   * Constructor.
   * @param i query info
   * @param prag pragmas
   * @param e enclosed expression
   */
  public Extension(final QueryInfo i, final Expr[] prag, final Expr e) {
    super(i, e);
    pragmas = prag;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // ignore pragma
    return expr.comp(ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return expr.iter(ctx);
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
    for(final Expr p : pragmas)
      sb.append(p).append(' ');
    return sb.append(BRACE1 + ' ' + expr + ' ' + BRACE2).toString();
  }
}
