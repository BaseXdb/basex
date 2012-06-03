package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;

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
  public FTDistance(final InputInfo ii, final FTExpr e, final Expr[] d, final FTUnit u) {
    super(ii, e);
    dist = d;
    unit = u;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(dist);
    super.checkUp();
  }

  @Override
  public FTExpr analyze(final QueryContext ctx) throws QueryException {
    for(int d = 0; d < dist.length; d++) dist[d] = dist[d].analyze(ctx);
    return super.analyze(ctx);
  }

  @Override
  public FTExpr compile(final QueryContext ctx) throws QueryException {
    for(int d = 0; d < dist.length; d++) dist[d] = dist[d].compile(ctx);
    return super.compile(ctx);
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
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DISTANCE, dist[0] + "-" + dist[1] + ' ' + unit), expr);
  }

  @Override
  public String toString() {
    return super.toString() + DISTANCE + PAR1 +
      dist[0] + '-' + dist[1] + ' ' + unit + PAR2;
  }
}
