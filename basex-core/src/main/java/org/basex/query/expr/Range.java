package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Range expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Range extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   */
  public Range(final InputInfo info, final Expr expr1, final Expr expr2) {
    super(info, expr1, expr2);
    seqType = SeqType.ITR_ZM;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    Expr e = this;
    if(oneIsEmpty()) {
      e = Empty.SEQ;
    } else if(allAreValues()) {
      e = value(qc);
    }
    return optPre(e, qc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long[] v = rng(qc);
    return v == null ? Empty.SEQ : RangeSeq.get(v[0], v[1] - v[0] + 1, true);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Range(info, exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs));
  }

  /**
   * Returns the start and end value of the range operator, or {@code null}
   * if the range could not be evaluated.
   * @param qc query context
   * @return value array
   * @throws QueryException query exception
   */
  private long[] rng(final QueryContext qc) throws QueryException {
    final Item it1 = exprs[0].item(qc, info);
    if(it1 == null) return null;
    final Item it2 = exprs[1].item(qc, info);
    if(it2 == null) return null;
    return new long[] { checkItr(it1), checkItr(it2) };
  }

  @Override
  public String toString() {
    return toString(' ' + TO + ' ');
  }
}
