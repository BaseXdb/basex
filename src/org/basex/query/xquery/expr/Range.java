package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.RangeIter;

/**
 * Range Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Range extends Arr {
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   */
  public Range(final Expr e1, final Expr e2) {
    super(e1, e2);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item i1 = ctx.iter(expr[0]).atomic(this, true);
    if(i1 == null) return Iter.EMPTY;
    final Item i2 = ctx.iter(expr[1]).atomic(this, true);
    if(i2 == null) return Iter.EMPTY;
    
    final long l1 = checkItr(i1);
    final long l2 = checkItr(i2);
    return l2 < l1 ? Iter.EMPTY : l2 == l1 ? Itr.iter(l1) :
      new RangeIter(l1, l2);
  }

  @Override
  public String toString() {
    return "Range(" + toString(" to ") + ")";
  }
}
