package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Expr;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTUnit;

/**
 * FTDistance expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTDistance extends FTFilter {
  /** Distance. */
  private final Expr[] dist;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param d distances
   * @param u unit
   */
  public FTDistance(final InputInfo ii, final FTExpr e, final Expr[] d,
      final FTUnit u) {
    super(ii, e);
    dist = d;
    unit = u;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    for(int d = 0; d != dist.length; ++d) dist[d] = dist[d].comp(ctx);
    return super.comp(ctx);
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final FTLexer lex) throws QueryException {

    final long min = checkItr(dist[0], ctx);
    final long max = checkItr(dist[1], ctx);
    mtc.sort();

    final FTMatch match = new FTMatch();
    FTStringMatch sm = null;
    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(m.ex) {
        match.add(m);
      } else {
        if(sm != null) {
          final int d = pos(m.s, lex) - pos(sm.e, lex) - 1;
          if(d < min || d > max) return false;
        } else {
          f = m;
        }
        sm = m;
      }
    }
    f.e = sm.e;
    mtc.reset();
    mtc.add(f);
    mtc.add(match);
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Expr d : dist) if(d.uses(u)) return true;
    return super.uses(u);
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final Expr d : dist) c += d.count(v);
    return c + super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr d : dist) if(!d.removable(v)) return false;
    return super.removable(v);
  }

  @Override
  public FTExpr remove(final Var v) {
    for(int d = 0; d != dist.length; ++d) dist[d] = dist[d].remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(QueryText.DISTANCE),
        token(dist[0] + "-" + dist[1] + ' ' + unit));
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + QueryText.DISTANCE + PAR1 +
      dist[0] + '-' + dist[1] + ' ' + unit + PAR2;
  }
}
