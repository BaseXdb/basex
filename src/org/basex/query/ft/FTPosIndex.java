package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.query.util.Err;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTPosIndex extends FTExpr {
  /** Position filter. */
  public FTPos pos;

  /**
   * Constructor.
   * @param e expression
   * @param p fulltext selections
   */
  public FTPosIndex(final FTExpr e, final FTPos p) {
    super(e);
    pos = p;
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) {
    final FTPos tmp = ctx.ftpos;

    return new FTNodeIter() {
      @Override
      public FTNodeItem next() throws QueryException {
        ctx.ftpos = pos;
        pos.init(ctx.ftitem);
        FTNodeItem it = expr[0].iter(ctx).next();
        if (ctx.ftpos != null) {
          if (it.ftn.size > 0) {
            init(it);
            while(!pos.filter(ctx)) {
              it = expr[0].iter(ctx).next();
              if(it.ftn.size == 0) {
                ctx.ftpos = tmp;
                return it;
              }
              init(it);
            } 
          }    
        }
        // calculate weight
        if(pos.weight != null) {
          final double d = checkDbl(pos.weight, ctx);
          if(d < 0 || d > 1000) Err.or(FTWEIGHT, d);
          if (d != -1) it.score(it.score() * d);
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
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr[0].toString();
  }
}
