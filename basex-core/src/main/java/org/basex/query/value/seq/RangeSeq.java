package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

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
   * @param size size
   * @param asc ascending
   * @return resulting item or sequence
   */
  public static Value get(final long min, final long size, final boolean asc) {
    return size < 1 ? Empty.SEQ : size == 1 ? Int.get(min) : new RangeSeq(min, size, asc);
  }

  /**
   * Returns the first value.
   * @return start value
   */
  public long start() {
    return start;
  }

  /**
   * Returns the last value.
   * @return end value
   */
  public long end() {
    return asc ? start + size - 1 : start - size + 1;
  }

  @Override
  public Object toJava() {
    final long[] obj = new long[(int) size];
    for(int s = 0; s < size; ++s) obj[s] = start + (asc ? s : -s);
    return obj;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw EBV_X.get(ii, this);
  }

  @Override
  public SeqType seqType() {
    return SeqType.ITR_OM;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof RangeSeq)) return false;
    final RangeSeq rs = (RangeSeq) cmp;
    return start == rs.start && size == rs.size && asc == rs.asc;
  }

  @Override
  public int writeTo(final Item[] arr, final int index) {
    for(int i = 0; i < size; i++) arr[index + i] = itemAt(i);
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
  public Value materialize(final InputInfo ii) {
    return this;
  }

  @Override
  public Value atomValue(final InputInfo ii) {
    return this;
  }

  @Override
  public long atomSize() {
    return size;
  }

  @Override
  public boolean homogeneous() {
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(FROM, start(), TO, end()));
  }

  @Override
  public String toString() {
    final long s = asc ? start : start - size + 1;
    final long e = asc ? start + size - 1 : start;
    final String str = PAREN1 + s + ' ' + TO + ' ' + e + PAREN2;
    return asc ? str : Function.REVERSE.args(str);
  }
}
