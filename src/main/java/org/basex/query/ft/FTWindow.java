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
import org.basex.util.Tokenizer.FTUnit;

/**
 * FTWindow expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    win = checkUp(win, ctx).comp(ctx);
    return super.comp(ctx);
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) throws QueryException {

    final int n = (int) checkItr(win, ctx) - 1;
    mtc.sort();

    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(m.n) continue;
      if(f == null) f = m;
      f.g |= m.e - f.e > 1;
      f.e = m.e;
      if(pos(f.e, ft) - pos(f.s, ft) > n) return false;
    }
    if(f == null) return false;

    final int w = n - pos(f.e, ft) + pos(f.s, ft);
    for(int s = pos(f.s, ft) - w; s <= pos(f.s, ft); s++) {
      boolean h = false;
      for(final FTStringMatch m : mtc) {
        h = m.n && pos(m.s, ft) >= s && pos(m.e, ft) <= s + w;
        if(h) break;
      }
      if(!h) {
        mtc.reset();
        mtc.add(f);
        return true;
      }
    }
    return false;
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

  /* [CG] XQFT: check sequential scan with NOT combinations
  @Override
  public boolean indexAccessible(final IndexContext ic) {
    return false;
  }*/

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(QueryTokens.WINDOW), token(unit.toString()));
    win.plan(ser);
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + QueryTokens.WINDOW + " " + win + " " + unit;
  }
}
