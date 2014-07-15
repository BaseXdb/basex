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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RangeSeq extends Seq {
  /** Start value. */
  private final long start;
  /** Ascending/descending. */
  private final boolean asc;

  /**
   * Constructor.
   * @param start start value
   * @param size size
   * @param asc ascending
   */
  private RangeSeq(final long start, final long size, final boolean asc) {
    super(size, AtomType.ITR);
    this.start = start;
    this.asc = asc;
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
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw CONDTYPE.get(ii, this);
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
    final long s = start;
    final long e = asc ? start + size - 1 : start - size + 1;
    addPlan(plan, planElem(FROM, s, TO, e));
  }

  @Override
  public String toString() {
    final long s = asc ? start : start - size + 1;
    final long e = asc ? start + size - 1 : start;
    final String str = PAR1 + s + ' ' + TO + ' ' + e + PAR2;
    return asc ? str : Function.REVERSE.args(str);
  }
}
