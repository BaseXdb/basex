package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.util.Tokenizer;

/**
 * FTOrder expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTOrder extends FTFilter {
  /**
   * Constructor.
   * @param e expression
   */
  public FTOrder(final FTExpr e) {
    super(e);
  }
  
  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) {

    int p = 0, s = 0;
    boolean f = true;
    for(final FTStringMatch sm : mtc) {
      if(sm.not) continue;
      if(f) {
        if(p == sm.queryPos) continue;
        p = sm.queryPos;
      }
      f = s <= sm.start;
      if(f) s = sm.start;
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
