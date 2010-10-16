package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.util.BoolList;
import org.basex.util.InputInfo;
import org.basex.util.Tokenizer;
import org.basex.util.Tokenizer.FTUnit;

/**
 * FTScope expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTScope extends FTFilter {
  /** Same/different flag. */
  private final boolean same;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param u unit
   * @param s same flag
   */
  public FTScope(final InputInfo ii, final FTExpr e, final FTUnit u,
      final boolean s) {
    super(ii, e);
    unit = u;
    same = s;
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) {

    if(same) {
      int s = -1;
      for(final FTStringMatch sm : mtc) {
        if(sm.ex) continue;
        final int p = pos(sm.s, ft);
        if(s == -1) s = p;
        else if(s != p) return false;
      }
      return true;
    }
    int c = 0;
    final BoolList bl = new BoolList();
    for(final FTStringMatch sm : mtc) {
      if(sm.ex) continue;
      c++;
      final int p = pos(sm.s, ft);
      final int s = bl.size();
      if(p < s && bl.get(p) && p == pos(sm.e, ft)) return false;
      bl.set(true, p);
    }
    return c > 1;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(same ? SAME : DIFFERENT),
        token(unit.toString()));
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + (same ? SAME : DIFFERENT) + " " + unit;
  }
}
