package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;

/**
 * FTSelectIndex expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTSelectIndex extends FTExpr {
  /** Position filter. */
  public FTSelect sel;

  /**
   * Constructor.
   * @param s full-text selections
   */
  public FTSelectIndex(final FTSelect s) {
    super();
    sel = s;
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) {
    final FTSelect tmp = ctx.ftselect;
    final FTExpr e = sel.expr[0];

    return new FTNodeIter() {
      @Override
      public FTNodeItem next() throws QueryException {
        ctx.ftselect = sel;
        sel.init(ctx.ftitem);
        FTNodeItem it = e.iter(ctx).next();
        if(ctx.ftselect != null && it.ftn.size > 0) {
          init(it);
          while(!sel.filter(ctx)) {
            it = e.iter(ctx).next();
            if(it.ftn.size == 0) {
              ctx.ftselect = tmp;
              return it;
            }
            init(it);
          }
        }
        ctx.ftselect = tmp;
        return it;
      }

      /**
       * Initializes item for next seqEval with index use.
       * @param it current FTNode 
       */
      void init(final FTNodeItem it) {
        sel.setPos(it.ftn.convertPos(), it.ftn.p.list[0]);
        if(it.ftn.getToken() != null) {
          sel.ft.init(it.data.text(it.ftn.getPre()));
          sel.term = sel.ft.getTokenList();
        }
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    sel.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return sel.toString();
  }
}
