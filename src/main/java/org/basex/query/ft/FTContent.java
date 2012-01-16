package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.util.InputInfo;
import org.basex.util.ft.FTLexer;

/**
 * FTContent expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTContent extends FTFilter {
  /** Start flag. */
  private final boolean start;
  /** End flag. */
  private final boolean end;

  /**
   * Constructor.
   * @param ii input info
   * @param ex expression
   * @param s start flag
   * @param e end flag
   */
  public FTContent(final InputInfo ii, final FTExpr ex, final boolean s,
      final boolean e) {
    super(ii, ex);
    start = s;
    end = e;
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc,
      final FTLexer lex) {
    if(start) {
      for(final FTStringMatch sm : mtc) if(sm.s == 0) return true;
    } else if(end) {
      final int p = lex.count() - 1;
      for(final FTStringMatch sm : mtc) if(sm.e == p) return true;
    } else {
      final int s = lex.count();
      final boolean[] bl = new boolean[s];
      for(final FTStringMatch sm : mtc) {
        if(sm.g) continue;
        for(int p = sm.s; p <= sm.e; ++p) bl[p] = true;
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
    ser.openElement(this, token(start ? START : end ? END : CONTENT), TRUE);
    super.plan(ser);
  }

  @Override
  public String toString() {
    return super.toString() + (start || end ? AT + " " +
        (start ? START : END) : ENTIRE + " " + CONTENT);
  }
}
