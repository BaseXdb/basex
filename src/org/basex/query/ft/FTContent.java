package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.QueryContext;
import org.basex.query.QueryTokens;
import org.basex.query.item.FTItem;

/**
 * FTContent expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTContent extends FTFilter {
  /** Start flag. */
  private final boolean start;
  /** End flag. */
  private final boolean end;
  
  /**
   * Constructor.
   * @param s start flag
   * @param e end flag
   */
  public FTContent(final boolean s, final boolean e) {
    start = s;
    end = e;
  }
  
  @Override
  boolean filter(final QueryContext ctx, final FTItem n, final Tokenizer ft) {
    if(start || end) {
      final int p = start ? 0 : ft.count() - 1;
      for(int i = 0; i < n.pos.length; i++) {
        for(int j = 0; j < n.pos[i].size; j++) {
          if(n.pos[i].list[j] == p) return true;
        }
      }
      return false;
    }

    final int s = ft.count();
    final boolean[] bl = new boolean[s];
    for(int i = 0; i < n.pos.length; i++) {
      for(int j = 0; j < n.pos[i].size; j++) bl[n.pos[i].list[j]] = true;
    }
    for(final boolean b : bl) if(!b) return false;
    return true;
  }

  @Override
  boolean content() {
    return end || !start;
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.attribute(token(start ? QueryTokens.START : end ? QueryTokens.END :
      QueryTokens.CONTENT), TRUE);
  }

  @Override
  public String toString() {
    return start || end ? QueryTokens.AT + " " + (start ? QueryTokens.START : 
      QueryTokens.END) : QueryTokens.ENTIRE + " " + QueryTokens.CONTENT;
  }
}
