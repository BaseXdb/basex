package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.RangeSeq;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Range expression.
 *
 * @author BaseX Team 2005-11, BSD License
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
        size = v[1] - v[0] + 1;
        // use iterative evaluation at runtime instead of range sequence
        // to avoid prevent intermediary result materialization
        e = size < 1 ? Empty.SEQ : size == 1 ? Int.get(v[0]) : this;
      }
    }
    type = SeqType.ITR_OM;
    return optPre(e, ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final long[] v = rng(ctx);
    return v == null || v[0] > v[1] ? Empty.SEQ :
      v[0] == v[1] ? Int.get(v[0]) : new RangeSeq(v[0], v[1] - v[0] + 1);
  }

  /**
   * Returns the start and end value of the range operator, or {@code null}
   * if the range could not be evaluated.
   * @param ctx query context
   * @return value array
   * @throws QueryException query exception
   */
  long[] range(final QueryContext ctx) throws QueryException {
    return values() ? rng(ctx) : null;
  }

  /**
   * Returns the start and end value of the range operator, or {@code null}
   * if the range could not be evaluated.
   * @param ctx query context
   * @return value array
   * @throws QueryException query exception
   */
  private long[] rng(final QueryContext ctx) throws QueryException {
    final Item a = expr[0].item(ctx, input);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, input);
    if(b == null) return null;
    return new long[] { checkItr(a), checkItr(b) };
  }

  @Override
  public String toString() {
    return PAR1 + toString(" " + QueryText.TO + " ") + PAR2;
  }
}
