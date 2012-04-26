package org.basex.query.ft;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * FTWindow expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTWindow extends FTFilter {
  /** Window. */
  private Expr win;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param w window
   * @param u unit
   */
  public FTWindow(final InputInfo ii, final FTExpr e, final Expr w,
      final FTUnit u) {
    super(ii, e);
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
      final FTLexer lex) throws QueryException {

    final int n = (int) checkItr(win, ctx) - 1;
    mtc.sort();

    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(m.ex) continue;
      if(f == null) f = m;
      f.g |= m.e - f.e > 1;
      f.e = m.e;
      if(pos(f.e, lex) - pos(f.s, lex) > n) return false;
    }
    if(f == null) return false;

    final int w = n - pos(f.e, lex) + pos(f.s, lex);
    for(int s = pos(f.s, lex) - w; s <= pos(f.s, lex); ++s) {
      boolean h = false;
      for(final FTStringMatch m : mtc) {
        h = m.ex && pos(m.s, lex) >= s && pos(m.e, lex) <= s + w;
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
  public boolean uses(final Use u) {
    return win.uses(u) || super.uses(u);
  }

  @Override
  public int count(final Var v) {
    return win.count(v) + super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return win.removable(v) && super.removable(v);
  }

  @Override
  public FTExpr remove(final Var v) {
    win = win.remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(QueryText.WINDOW), token(unit.toString()));
    win.plan(ser);
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + QueryText.WINDOW + ' ' + win + ' ' + unit;
  }
}
