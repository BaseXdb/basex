package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;

/**
 * And expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class And extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public And(final Expr[] l) {
    super(l);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(!it.bool()) return Bln.FALSE.iter();
      d = Scoring.and(d, it.score());
    }
    return new Bln(true, d).iter();
  }

  @Override
  public String toString() {
    return toString(" and ");
  }
}
