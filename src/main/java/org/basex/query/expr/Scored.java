package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;

/**
 * Scored expression. This is a proprietary extension to XQuery FT introduced by
 * Stefan Klinger.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leo Woerteler
 */
public class Scored extends Arr {
  /**
   * Constructor.
   * @param e scored expression
   * @param score expression computing the score
   */
  public Scored(final Expr e, final Expr score) {
    super(e, score);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // ignore pragma
    return expr[0].comp(ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return expr[0].iter(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return toString(SCORED);
  }
}
