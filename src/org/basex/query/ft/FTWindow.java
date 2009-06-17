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
  boolean filter(final QueryContext ctx, final FTMatch mtc, final Tokenizer ft)
      throws QueryException {

    final long win = checkItr(expr[0], ctx);
    mtc.sort();

    int end = 0;
    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(!m.not) {
        if(f == null) f = m;
        end = m.end;
      }
    }
    f.end = end;

    final FTMatch match = new FTMatch();
    for(final FTStringMatch m : mtc) {
      if(m.not) {
        match.add(m);
      } else {
        if(pos(end, ft) - pos(m.start, ft) + 1 > win) return false;
        break;
      }
    }

    mtc.reset();
    mtc.add(f);
    mtc.add(match);

    return true;
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
