package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.expr.Expr;
import org.basex.query.item.FTItem;

/**
 * FTWindow expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTWindow extends FTFilter {
  /**
   * Constructor.
   * @param w window
   * @param u unit
   */
  public FTWindow(final Expr w, final FTUnit u) {
    expr = new Expr[] { w };
    unit = u;
  }

  @Override
  boolean filter(final QueryContext ctx, final FTItem n, final Tokenizer ft)
      throws QueryException {
    return checkDist(1, checkItr(expr[0], ctx), false, n.pos, ft);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.attribute(token(QueryTokens.WINDOW), token(expr[0] + " " + unit));
  }

  @Override
  public String toString() {
    return QueryTokens.WINDOW + "(" + expr[0] + " " + unit + ")";
  }
}
