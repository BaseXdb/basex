package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.expr.Expr;
import org.basex.util.Tokenizer;

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
  boolean filter(final QueryContext ctx, final FTMatch mtc, final Tokenizer ft)
      throws QueryException {

    final long min = checkItr(expr[0], ctx);
    final long max = checkItr(expr[1], ctx);
    mtc.sort();

    final FTMatch match = new FTMatch();
    FTStringMatch sm = null;
    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(m.not) {
        match.add(m);
      } else {
        if(sm != null) {
          final int d = pos(m.start, ft) - pos(sm.end, ft) - 1;
          if(d < min || d > max) return false;
        } else {
          f = m;
        }
        sm = m;
      }
    }
    f.end = sm.end;
    mtc.reset();
    mtc.add(f);
    mtc.add(match);
    return true;
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
