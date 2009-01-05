package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Seq;
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
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    if(expr[0].e() || expr[1].e()) {
      ctx.compInfo(OPTSIMPLE, this, Seq.EMPTY);
      return Seq.EMPTY;
    }
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item i1 = atomic(ctx, expr[0], true);
    if(i1 == null) return Iter.EMPTY;
    final Item i2 = atomic(ctx, expr[1], true);
    if(i2 == null) return Iter.EMPTY;
    
    final long l1 = checkItr(i1);
    final long l2 = checkItr(i2);
    return l2 < l1 ? Iter.EMPTY : l1 == l2 ? Itr.get(l1).iter() :
      new RangeIter(l1, l2);
  }

  @Override
  public String toString() {
    return "Range(" + toString(" to ") + ")";
  }
}
