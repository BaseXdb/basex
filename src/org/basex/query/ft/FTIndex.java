package org.basex.query.ft;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTTokenizer;
import org.basex.index.IndexArrayIterator;
import org.basex.query.QueryContext;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;

/**
 * This expression retrieves the ids of indexed fulltext terms.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTIndex extends FTExpr {
  /** Fulltext token. */
  final byte[] tok;
  /** Data reference. */
  final Data data;
  /** Index iterator. */
  IndexArrayIterator iat;

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
  public FTNodeIter iter(final QueryContext ctx) {
    return new FTNodeIter() {
      public final FTOpt fto = ctx.ftopt;

      @Override
      public FTNodeItem next() { 
        if(iat == null && !evalIter()) {
          return new FTNodeItem();
        }
        return new FTNodeItem(iat.more() ? iat.nextFTNode() :
          new FTNode(), data);
      }

      /**
       * Evaluates the index access.
       * @return true if result was found
       */
      private boolean evalIter() {
        final FTTokenizer ft = new FTTokenizer(tok, fto);
        ft.lp = true;
        while(ft.more()) {
          if(data.nrIDs(ft) == 0) return false;
          ctx.checkStop();
        }
        ft.init();
        int w = 0;
        while(ft.more()) {
          final IndexArrayIterator it = (IndexArrayIterator) data.ids(ft);
          iat = w == 0 ? it : IndexArrayIterator.and(iat, it, w);
          w++;
        }
        
        if(iat != null && iat.size() > 0) {
          iat.setToken(new FTTokenizer[]{ft});
          iat.setTokenNum(++ctx.ftcount);
          return true;
        }
        return false;
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
