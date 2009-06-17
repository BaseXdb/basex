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

/**
 * FTScope expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTScope extends FTFilter {
  /** Same/different flag. */
  private final boolean same;

  /**
   * Constructor.
   * @param u unit
   * @param s same flag
   */
  public FTScope(final FTUnit u, final boolean s) {
    unit = u;
    same = s;
  }

  @Override
  boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) {
    if(same) {
      int s = -1;
      for(final FTStringMatch sm : mtc) {
        if(sm.not) continue;
        final int p = pos(sm.start, ft);
        if(s == -1) s = p;
        else if(s != p) return false;
      }
    } else {
      final BoolList bl = new BoolList();
      for(final FTStringMatch sm : mtc) {
        if(sm.not) continue;
        final int p = pos(sm.start, ft);
        if(bl.list[p]) return false;
        bl.set(true, p);
      }
    }
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.attribute(token(same ? QueryTokens.SAME : QueryTokens.DIFFERENT),
        token(unit.toString()));
  }

  @Override
  public String toString() {
    return (same ? QueryTokens.SAME : QueryTokens.DIFFERENT) + " " + unit;
  }
}
