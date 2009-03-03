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

    Expr e = this;
    if(expr[0].e() || expr[1].e()) {
      e = Seq.EMPTY;
    } else {
      final long[] v = range(ctx);
      if(v != null && v[0] == v[1]) e = Itr.get(v[0]);
    }
    if(e != this) ctx.compInfo(OPTPRE, this);
    return e;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final long[] v = rng(ctx);
    if(v == null) return Iter.EMPTY;
    return v[0] > v[1] ? Iter.EMPTY : v[0] == v[1] ? Itr.get(v[0]).iter() :
      new RangeIter(v[0], v[1]);
  }

  @Override
  public long size(final QueryContext ctx) throws QueryException {
    if(expr[0].i() && expr[1].i()) {
      final long[] v = rng(ctx);
      if(v[1] >= v[0]) return v[1] - v[0] + 1;
    }
    return -1;
  }

  /**
   * Returns the start and end value of the range operator, or null if
   * the range could not be evaluated.
   * @param ctx query context
   * @return value array
   * @throws QueryException Exception
   */
  public long[] range(final QueryContext ctx) throws QueryException {
    return expr[0].i() && expr[1].i() ? rng(ctx) : null;
  }

  /**
   * Returns the start and end value of the range operator, or null if
   * the range could not be evaluated.
   * @param ctx query context
   * @return value array
   * @throws QueryException Exception
   */
  private long[] rng(final QueryContext ctx) throws QueryException {
    final Item a = expr[0].atomic(ctx);
    if(a == null) return null;
    final Item b = expr[1].atomic(ctx);
    if(b == null) return null;
    return new long[] { checkItr(a), checkItr(b) };
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
