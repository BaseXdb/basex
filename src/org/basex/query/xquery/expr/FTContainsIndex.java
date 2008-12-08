package org.basex.query.xquery.expr;

import org.basex.index.FTTokenizer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;

/**
 * FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContainsIndex extends Arr {
  /** Fulltext parser. */
  public final FTTokenizer ft;

  /**
   * Constructor.
   * @param ftt FTTokenizer
   * @param ex contains, select and optional ignore expression
   */
  public FTContainsIndex(final FTTokenizer ftt, final Expr... ex) {
    super(ex);
    ft = ftt;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter(){
      @Override
      public Item next() throws XQException {
        final FTTokenizer tmp = ctx.ftitem;
        ctx.ftitem = ft;
        final Item i1 = ctx.iter(expr[1]).next();
        ctx.ftitem = tmp;
        if (i1.dbl() == 0) return null;
        return  ((FTNodeItem) i1).getDNode();
      }
    };
  
  }


  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public String color() {
    return "33CC33";
  }

  @Override
  public String toString() {
    return toString(" ftcontainsIndex ");
  }
}
