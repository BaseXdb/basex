package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.FTTokenizer;

/**
 * FTContains expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTCont extends Arr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();

  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   */
  public FTCont(final Expr... ex) {
    super(ex);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(expr[0]);
    final FTTokenizer tmp = ctx.ftitem;

    double d = 0;
    Item i;
    ctx.ftitem = ft;
    while((i = iter.next()) != null) {
      ft.init(i.str());
      final Item it = ctx.iter(expr[1]).next();
      d = Scoring.and(d, it.dbl());
    }
    ctx.ftitem = tmp;
    return d == 0 ? Iter.EMPTY : new Bln(true, d).iter();
  }

  @Override
  public String toString() {
    return toString(" ftcontains ");
  }

  @Override
  public Type returned() {
    return Type.BLN;
  }
}
