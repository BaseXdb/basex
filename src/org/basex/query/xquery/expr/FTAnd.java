package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;

/**
 * FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTAnd extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public FTAnd(final Expr[] e) {
    super(e);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).next();
      if(!it.bool()) return Dbl.iter(0);
      d = Scoring.and(d, it.dbl());
    }
    return Dbl.iter(d);
  }

  @Override
  public String toString() {
    return toString(" ftand ");
  }
}
