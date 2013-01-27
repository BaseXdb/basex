package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Range expression.
 *
 * @author BaseX Team 2005-12, BSD License
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
    type = SeqType.ITR_OM;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);

    Expr e = this;
    if(oneIsEmpty()) {
      e = Empty.SEQ;
    } else if(allAreValues()) {
      e = value(ctx);
    }
    return optPre(e, ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final long[] v = rng(ctx);
    return v == null ? Empty.SEQ : RangeSeq.get(v[0], v[1] - v[0] + 1, true);
  }

  /**
   * Returns the start and end value of the range operator, or {@code null}
   * if the range could not be evaluated.
   * @param ctx query context
   * @return value array
   * @throws QueryException query exception
   */
  private long[] rng(final QueryContext ctx) throws QueryException {
    final Item a = expr[0].item(ctx, info);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, info);
    if(b == null) return null;
    return new long[] { checkItr(a), checkItr(b) };
  }

  @Override
  public String toString() {
    return toString(' ' + TO + ' ');
  }
}
