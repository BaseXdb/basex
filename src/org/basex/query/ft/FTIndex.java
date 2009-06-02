package org.basex.query.ft;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.index.FTEntry;
import org.basex.index.FTIndexIterator;
import org.basex.query.QueryContext;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;

/**
 * This expression retrieves the ids of indexed full-text terms.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTIndex extends FTExpr {
  /** Data reference. */
  final Data data;
  /** Full-text token. */
  final byte[] tok;

  /**
   * Constructor.
   * @param d data reference
   * @param t token
   */
  public FTIndex(final Data d, final byte[] t) {
    data = d;
    tok = t;
  }

  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      /** Index iterator. */
      FTIndexIterator iat;

      @Override
      public FTItem next() {
        if(iat == null) {
          final Tokenizer ft = new Tokenizer(tok, ctx.ftopt);
          ft.lp = true;
          ft.init();
          int w = 0;
          while(ft.more()) {
            final FTIndexIterator it = (FTIndexIterator) data.ids(ft);
            iat = w == 0 ? it : FTIndexIterator.intersect(iat, it, w);
            w++;
          }
          iat.setTokenNum(++ctx.ftcount);
        }
        return new FTItem(iat.more() ? iat.node() : new FTEntry(), data);
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.text(tok);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return BaseX.info("%(\"%\")", name(), tok);
  }
}
