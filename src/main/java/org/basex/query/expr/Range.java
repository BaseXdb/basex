package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.RangeIter;
import org.basex.util.InputInfo;

/**
 * Range expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Range extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   */
  public Range(final InputInfo ii, final Expr e1, final Expr e2) {
    super(ii, e1, e2);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    Expr e = this;
    if(oneEmpty()) {
      e = Empty.SEQ;
    } else {
      final long[] v = range(ctx);
      if(v != null) {
        if(v[0] >  v[1]) e = Empty.SEQ;
        if(v[0] == v[1]) e = Itr.get(v[0]);
        size = v[0] <= v[1] ? v[1] - v[0] + 1 : 0;
      }
    }
    type = SeqType.ITR_OM;
    return optPre(e, ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final long[] v = rng(ctx);
    return v == null || v[0] > v[1] ? Iter.EMPTY :
      v[0] == v[1] ? Itr.get(v[0]).iter(ctx) : new RangeIter(v[0], v[1]);
  }

  /**
   * Returns the start and end value of the range operator, or {@code null} if
   * the range could not be evaluated.
   * @param ctx query context
   * @return value array
   * @throws QueryException query exception
   */
  long[] range(final QueryContext ctx) throws QueryException {
    return values() ? rng(ctx) : null;
  }

  /**
   * Returns the start and end value of the range operator, or null if the
   * range could not be evaluated.
   * @param ctx query context
   * @return value array
   * @throws QueryException query exception
   */
  private long[] rng(final QueryContext ctx) throws QueryException {
    final Item a = expr[0].atomic(ctx, input);
    if(a == null) return null;
    final Item b = expr[1].atomic(ctx, input);
    if(b == null) return null;
    return new long[] { checkItr(a), checkItr(b) };
  }

  @Override
  public String toString() {
    return toString(" " + QueryTokens.TO + " ");
  }
}
