package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
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
  FTSelectIndex(final FTSelect s) {
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
        
        FTNode it;
        while(!(it = ir.next()).empty() && !ctx.ftselect.standard()) {
          it.setPos();
          sel.ft = new Tokenizer(it.data.text(it.fte.pre()));
          if(sel.filter(ctx, it)) break;
        }

        ctx.ftselect = tmp;
        return it;
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
