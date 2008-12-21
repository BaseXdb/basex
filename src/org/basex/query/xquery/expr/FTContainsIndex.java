package org.basex.query.xquery.expr;

import org.basex.index.FTTokenizer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;

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
  FTContainsIndex(final Expr ex, final FTTokenizer ftt) {
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
        final Item it = ctx.iter(expr[0]).next();
        ctx.ftitem = tmp;
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
    return "FTContainsIndex(" + expr[0] + ")";
  }
}
