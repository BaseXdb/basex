package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Range sequence, containing at least two integers.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class RangeSeq extends Seq {
  /** Start value. */
  private final long start;
  /** Ascending/descending order. */
  public final boolean asc;

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
   * Returns the range as long values.
   * @param order respect ascending/descending order
   * @return start value
   */
  public long[] range(final boolean order) {
    final long end = asc ? start + size - 1 : start - size + 1;
    return new long[] { order || asc ? start : end, order || asc ? end : start };
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
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof RangeSeq)) return super.equals(obj);
    final RangeSeq rs = (RangeSeq) obj;
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
  public Value insert(final long pos, final Item item) {
    return copyInsert(pos, item);
  }

  @Override
  public Value remove(final long pos) {
    return pos == 0 || pos == size - 1 ? subSeq(pos == 0 ? 0 : 1, size - 1) : copyRemove(pos);
  }

  @Override
  public Value reverse() {
    return get(range(true)[1], size(), !asc);
  }

  @Override
  public void materialize(final InputInfo ii) { }

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
    final long[] range = range(true);
    addPlan(plan, planElem(FROM, range[0], TO, range[1], SIZE, size, TYPE, seqType()));
  }

  @Override
  public String description() {
    return "range " + SEQUENCE;
  }

  @Override
  public String toString() {
    final long[] range = range(false);
    final String str = PAREN1 + range[0] + ' ' + TO + ' ' + range[1] + PAREN2;
    return asc ? str : Function.REVERSE.args(' ' + str).substring(1);
  }
}
