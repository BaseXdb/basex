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
 * FTDistance expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTDistance extends FTFilter {
  /**
   * Constructor.
   * @param d distances
   * @param u unit
   */
  public FTDistance(final Expr[] d, final FTUnit u) {
    expr = d;
    unit = u;
  }

  @Override
  boolean filter(final QueryContext ctx, final FTItem node, final Tokenizer ft)
      throws QueryException {
    return checkDist(checkItr(expr[0], ctx), checkItr(expr[1], ctx), true,
        node.pos, ft);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.attribute(token(QueryTokens.DISTANCE),
        token(expr[0] + "-" + expr[1] + " " + unit));
  }

  @Override
  public String toString() {
    return QueryTokens.DISTANCE + "(" +
      expr[0] + "-" + expr[1] + " " + unit + ")";
  }
}
