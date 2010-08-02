package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.util.InputInfo;
import org.basex.util.Tokenizer;

/**
 * FTOrder expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTOrder extends FTFilter {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  public FTOrder(final InputInfo ii, final FTExpr e) {
    super(ii, e);
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) {

    int p = 0, s = 0;
    boolean f = true;
    for(final FTStringMatch sm : mtc) {
      if(sm.n) continue;
      if(f) {
        if(p == sm.q) continue;
        p = sm.q;
      }
      f = s <= sm.s;
      if(f) s = sm.s;
    }
    return f;
  }

  @Override
  protected boolean content() {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(QueryTokens.ORDERED), TRUE);
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + QueryTokens.ORDERED;
  }
}
