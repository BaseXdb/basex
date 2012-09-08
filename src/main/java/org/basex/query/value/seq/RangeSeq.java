package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Range sequence, containing at least two integers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RangeSeq extends Seq {
  /** Start value. */
  private final long start;

  /**
   * Constructor.
   * @param s start value
   * @param sz size
   */
  public RangeSeq(final long s, final long sz) {
    super(sz);
    start = s;
  }

  @Override
  public Object toJava() {
    final long[] obj = new long[(int) size];
    for(int s = 0; s < size; ++s) obj[s] = start + s;
    return obj;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) throws QueryException {
    throw CONDTYPE.thrw(ii, this);
  }

  @Override
  public SeqType type() {
    return SeqType.ITR_OM;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof RangeSeq)) return false;
    final RangeSeq is = (RangeSeq) cmp;
    return start == is.start && size == is.size;
  }

  @Override
  public int writeTo(final Item[] arr, final int pos) {
    for(int i = 0; i < size; i++) arr[pos + i] = itemAt(i);
    return (int) size;
  }

  @Override
  public Item itemAt(final long pos) {
    return Int.get(start + pos);
  }

  @Override
  public boolean homogeneous() {
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, start, MAX, start + size - 1));
  }

  @Override
  public String toString() {
    return PAR1 + start + ' ' + TO + ' ' + (start + size - 1) + PAR2;
  }
}
