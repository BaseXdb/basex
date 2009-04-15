package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTPosIndex extends FTExpr {
  /** Position filter. */
  public FTPos pos;

  /**
   * Constructor.
   * @param p fulltext selections
   */
  public FTPosIndex(final FTPos p) {
    super();
    pos = p;
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) {
    final FTPos tmp = ctx.ftpos;
    final FTExpr e = pos.expr[0];

    return new FTNodeIter() {
      @Override
      public FTNodeItem next() throws QueryException {
        ctx.ftpos = pos;
        pos.init(ctx.ftitem);
        FTNodeItem it = e.iter(ctx).next();
        if (ctx.ftpos != null) {
          if (it.ftn.size > 0) {
            init(it);
            while(!pos.filter(ctx)) {
              it = e.iter(ctx).next();
              if(it.ftn.size == 0) {
                ctx.ftpos = tmp;
                return it;
              }
              init(it);
            } 
          }    
        }
        ctx.ftpos = tmp;
        return it;
      }

      /**
       * Init FTPos for next seqEval with index use.
       * @param it current FTNode 
       */
      void init(final FTNodeItem it) {
        pos.setPos(it.ftn.convertPos(), it.ftn.p.list[0]);
        if (it.ftn.getToken() != null) {
          pos.ft.init(it.data.text(it.ftn.getPre()));
          pos.term = pos.ft.getTokenList();
        }
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    pos.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return pos.toString();
  }
}
