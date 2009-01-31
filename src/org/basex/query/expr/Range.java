package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.RangeIter;

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
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(expr[0].e() || expr[1].e()) {
      ctx.compInfo(OPTPRE, this);
      return Seq.EMPTY;
    }
    if(expr[0].i() && expr[1].i()) {
      final long[] v = range(expr);
      if(v[0] == v[1]) return Itr.get(v[0]);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item i1 = atomic(ctx, expr[0], true);
    if(i1 == null) return Iter.EMPTY;
    final Item i2 = atomic(ctx, expr[1], true);
    if(i2 == null) return Iter.EMPTY;
    
    final long[] v = range(i1, i2);
    return v[0] > v[1] ? Iter.EMPTY : v[0] == v[1] ? Itr.get(v[0]).iter() :
      new RangeIter(v[0], v[1]);
  }

  @Override
  public long size(final QueryContext ctx) throws QueryException {
    if(expr[0].i() && expr[1].i()) {
      final long[] v = range(expr);
      if(v[1] >= v[0]) return v[1] - v[0] + 1;
    }
    return -1;
  }

  /**
   * Returns the start and end value of the range operator.
   * @param e range operands
   * @return value array
   * @throws QueryException Exception
   */
  private long[] range(final Expr... e) throws QueryException {
    return new long[] { checkItr((Item) e[0]), checkItr((Item) e[1]) };
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.NUMSEQ;
  }

  @Override
  public String toString() {
    return "Range(" + toString(" to ") + ")";
  }
}
