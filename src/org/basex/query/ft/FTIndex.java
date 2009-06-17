package org.basex.query.ft;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.FTIndexIterator;
import org.basex.query.QueryContext;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.Tokenizer;

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
  final byte[] txt;
  /** Single evaluation. */
  boolean fast;

  /**
   * Constructor.
   * @param d data reference
   * @param t text
   * @param f fast evaluation
   */
  public FTIndex(final Data d, final byte[] t, final boolean f) {
    data = d;
    txt = t;
    fast = f;
  }

  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      /** Index iterator. */
      FTIndexIterator iat;

      @Override
      public FTItem next() {
        if(iat == null) {
          final Tokenizer ft = new Tokenizer(txt, ctx.ftopt, fast);
          // more than one token: deactivate fast processing
          ft.fast &= ft.count() == 1;
          ft.init();
          while(ft.more()) {
            final FTIndexIterator it = (FTIndexIterator) data.ids(ft);
            iat = iat == null ? it : FTIndexIterator.intersect(iat, it);
          }
          iat.setTokenNum(++ctx.ftoknum);
        }
        return iat.more() ? new FTItem(iat.matches(), data, iat.next()) : null;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.text(txt);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return BaseX.info("%(\"%\")", name(), txt);
  }
}
