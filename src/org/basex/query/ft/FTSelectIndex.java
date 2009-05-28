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
      public FTNodeItem next() throws QueryException {
        ctx.ftselect = sel;
        sel.init(ctx.ftitem);
        FTNodeItem it = ir.next();

        if(!it.ftn.empty() && !ctx.ftselect.standard()) {
          init(it);
          while(!sel.filter(ctx)) {
            it = ir.next();
            if(it.ftn.empty()) break;
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
      void init(final FTNodeItem it) {
        sel.pos = it.ftn.convertPos();
        sel.size = it.ftn.p.list[0];
        if(it.ftn.getToken() != null) {
          sel.ft.init(it.data.text(it.ftn.pre()));
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
