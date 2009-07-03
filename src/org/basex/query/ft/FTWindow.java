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
import org.basex.query.util.Var;
import org.basex.util.Tokenizer;

/**
 * FTWindow expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTWindow extends FTFilter {
  /** Window. */
  private Expr win;
  
  /**
   * Constructor.
   * @param e expression
   * @param w window
   * @param u unit
   */
  public FTWindow(final FTExpr e, final Expr w, final FTUnit u) {
    super(e);
    win = w;
    unit = u;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    win = win.comp(ctx);
    return super.comp(ctx);
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) throws QueryException {

    final long w = checkItr(win, ctx);
    mtc.sort();

    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(!m.not) {
        if(f == null) f = m;
        f.gaps |= m.end - f.end > 1;
        f.end = m.end;
      }
    }

    final FTMatch match = new FTMatch();
    for(final FTStringMatch m : mtc) {
      if(m.not) {
        match.add(m);
      } else {
        if(pos(f.end, ft) - pos(m.start, ft) + 1 > w) return false;
        break;
      }
    }

    mtc.reset();
    mtc.add(f);
    mtc.add(match);

    return true;
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    return win.removable(v, ctx) && super.removable(v, ctx);
  }

  @Override
  public FTExpr remove(final Var v) {
    win = win.remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(QueryTokens.WINDOW), token(win + " " + unit));
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + QueryTokens.WINDOW + " " + win + " " + unit;
  }
}
