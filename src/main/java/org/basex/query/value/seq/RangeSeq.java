package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
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
  public final long start;
  /** Ascending/descending. */
  public final boolean asc;

  /**
   * Constructor.
   * @param s start value
   * @param sz size
   * @param a ascending
   */
  private RangeSeq(final long s, final long sz, final boolean a) {
    super(sz);
    start = s;
    asc = a;
  }

  /**
   * Returns a value representation of the specified items.
   * @param min minimum value
   * @param sz size
   * @param asc ascending
   * @return resulting item or sequence
   */
  public static Value get(final long min, final long sz, final boolean asc) {
    return sz < 1 ? Empty.SEQ : sz == 1 ? Int.get(min) : new RangeSeq(min, sz, asc);
  }

  @Override
  public Object toJava() {
    final long[] obj = new long[(int) size];
    for(int s = 0; s < size; ++s) obj[s] = start + (asc ? s : -s);
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
    return start == is.start && size == is.size && asc == is.asc;
  }

  @Override
  public int writeTo(final Item[] arr, final int pos) {
    for(int i = 0; i < size; i++) arr[pos + i] = itemAt(i);
    return (int) size;
  }

  @Override
  public Item itemAt(final long pos) {
    return Int.get(start + (asc ? pos : -pos));
  }

  @Override
  public Value sub(final long s, final long sz) {
    return get(start + (asc ? s : -s), sz, asc);
  }

  @Override
  public Value reverse() {
    final long s = size();
    return asc ? get(start + s - 1, s, false) : get(start - s + 1, s, true);
  }

  @Override
  public boolean homogeneous() {
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, start, MAX, start + (asc ? size : -size) - 1));
  }

  @Override
  public String toString() {
    final String s = PAR1 + start + ' ' + TO + ' ' + (start + size - 1) + PAR2;
    return asc ? s : Function.REVERSE.args(s);
  }
}
