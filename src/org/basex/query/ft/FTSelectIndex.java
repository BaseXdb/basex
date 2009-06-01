package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTNodeIter;

/**
 * FTSelectIndex expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class FTSelectIndex extends FTExpr {
  /** Position filter. */
  final FTSelect sel;

  /**
   * Constructor.
   * @param s full-text selections
   */
  public FTSelectIndex(final FTSelect s) {
    sel = s;
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    final FTSelect tmp = ctx.ftselect;
    final FTNodeIter ir = sel.expr[0].iter(ctx);

    return new FTNodeIter() {
      @Override
      public FTNode next() throws QueryException {
        ctx.ftselect = sel;
        sel.init(ctx.fttoken);
        FTNode it = ir.next();

        if(!it.empty() && !ctx.ftselect.standard()) {
          init(it);
          while(!sel.filter(ctx)) {
            it = ir.next();
            if(it.empty()) break;
            init(it);
          }
        }
        ctx.ftselect = tmp;
        return it;
      }

      /**
       * Initializes item for next seqEval with index use.
       * @param it current node
       */
      void init(final FTNode it) {
        sel.pos = it.fte.convertPos();
        sel.size = it.fte.poi.list[0];
        if(it.fte.getToken() != null) {
          sel.ft.init(it.data.text(it.fte.pre()));
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
