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
 * FTContent expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTContent extends FTFilter {
  /** Start flag. */
  private final boolean start;
  /** End flag. */
  private final boolean end;
  
  /**
   * Constructor.
   * @param ex expression
   * @param s start flag
   * @param e end flag
   */
  public FTContent(final FTExpr ex, final boolean s, final boolean e) {
    super(ex);
    start = s;
    end = e;
  }
  
  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final Tokenizer ft) {
    if(start) {
      for(final FTStringMatch sm : mtc) if(sm.start == 0) return true;
    } else if(end) {
      final int p = ft.count() - 1;
      for(final FTStringMatch sm : mtc) if(sm.end == p) return true;
    } else {
      final int s = ft.count();
      final boolean[] bl = new boolean[s];
      for(final FTStringMatch sm : mtc) {
        if(sm.gaps) continue;
        for(int p = sm.start; p <= sm.end; p++) bl[p] = true;
      }
      for(final boolean b : bl) if(!b) return false;
      return true;
    }
    return false;
  }

  @Override
  protected boolean content() {
    return end || !start;
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, token(start ? QueryTokens.START : end ?
        QueryTokens.END : QueryTokens.CONTENT), TRUE);
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + (start || end ? QueryTokens.AT + " " +
        (start ? QueryTokens.START : QueryTokens.END) :
          QueryTokens.ENTIRE + " " + QueryTokens.CONTENT);
  }
}
