package org.basex.query.xquery.expr;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.IntList;

/**
 * FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContainsIndex extends FTContains {
  /**
   * Constructor.
   * @param ftt FTTokenizer
   * @param ex contains, select and optional ignore expression
   */
  FTContainsIndex(final FTExpr ex, final FTTokenizer ftt) {
    super(null, ex);
    ft = ftt;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter(){
      @Override
      public Item next() throws XQException {
        final FTTokenizer tmp = ctx.ftitem;
        ctx.ftitem = ft;
        final FTNodeItem it = ftexpr.iter(ctx).next();
        ctx.ftitem = tmp;
        final IntList[] pos =  it.ftn.convertPos();
        if (ctx.ftpos != null) 
          ctx.ftpos.setPos(it.ftn.convertPos(), pos.length);
        return it.score() == 0 ? null : it;
      }
    };
  }

  @Override
  public Type returned() {
    return Type.NOD;
  }

  @Override
  public String toString() {
    return "FTContainsIndex(" + ftexpr + ")";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ftexpr.plan(ser);
    ser.closeElement();
  }
}
