package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.util.BoolList;
import org.basex.util.Tokenizer;
import org.basex.util.Tokenizer.FTUnit;

/**
 * FTScope expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTScope extends FTFilter {
  /** Same/different flag. */
  private final boolean same;

  /**
   * Constructor.
   * @param e expression
   * @param u unit
   * @param s same flag
   */
  public FTScope(final FTExpr e, final FTUnit u, final boolean s) {
    super(e);
    unit = u;
    same = s;
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) {
    if(same) {
      int s = -1;
      for(final FTStringMatch sm : mtc) {
        if(sm.n) continue;
        final int p = pos(sm.s, ft);
        if(s == -1) s = p;
        else if(s != p) return false;
      }
    } else {
      final BoolList bl = new BoolList();
      for(final FTStringMatch sm : mtc) {
        if(sm.n) continue;
        final int p = pos(sm.s, ft);
        if(bl.list[p]) return false;
        bl.set(true, p);
      }
    }
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(same ? QueryTokens.SAME :
      QueryTokens.DIFFERENT), token(unit.toString()));
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + (same ? QueryTokens.SAME :
      QueryTokens.DIFFERENT) + " " + unit;
  }
}
