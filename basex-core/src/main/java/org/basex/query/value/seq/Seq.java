package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Seq extends Value {
  /** Length. */
  final long size;

  /**
   * Constructor.
   * @param size size
   */
  Seq(final long size) {
    this(size, AtomType.ITEM);
  }

  /**
   * Constructor, specifying a type.
   * @param size size
   * @param type type
   */
  Seq(final long size, final Type type) {
    super(type);
    this.size = size;
  }

  /**
   * Returns a value representation of the specified items.
   * @param value value
   * @param size size
   * @return resulting item or sequence
   */
  public static Value get(final Item[] value, final int size) {
    return get(value, size, null);
  }

  /**
   * Returns a value representation of the specified items.
   * @param value value
   * @param size size
   * @param type sequence type
   * @return resulting item or sequence
   */
  public static Value get(final Item[] value, final int size, final Type type) {
    return size == 0 ? Empty.SEQ : size == 1 ? value[0] : new ItemSeq(value, size, type);
  }

  @Override
  public Object toJava() throws QueryException {
    final Object[] obj = new Object[(int) size];
    for(int s = 0; s < size; s++) obj[s] = itemAt(s).toJava();
    return obj;
  }

  @Override
  public final long size() {
    return size;
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw SEQFOUND_X.get(ii, this);
  }

  @Override
  public final Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    return ebv(qc, ii);
  }

  @Override
  public final ValueIter iter() {
    return new ValueIter() {
      int c;
      @Override
      public Item get(final long i) { return itemAt(i); }
      @Override
      public Item next() { return c < size ? itemAt(c++) : null; }
      @Override
      public boolean reset() { c = 0; return true; }
      @Override
      public long size() { return size; }
      @Override
      public Value value() { return Seq.this; }
    };
  }

  @Override
  public final int hash(final InputInfo ii) throws QueryException {
    // final hash function because equivalent sequences *must* produce the
    // same hash value, otherwise they get lost in hash maps.
    // example: hash(RangeSeq(1 to 3)) == hash(ItrSeq(1, 2, 3))
    //                                 == hash(ItemSeq(Itr(1), Itr(2), Itr(3)))
    int h = 1;
    for(long v = Math.min(size, 5); --v >= 0;) h = 31 * h + itemAt(v).hash(ii);
    return h;
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw SEQFOUND_X.get(ii, this);
  }

  /**
   * Returns a sequence in reverse order.
   * @return sequence
   */
  public abstract Value reverse();

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(SIZE, size);
    addPlan(plan, el);
    for(int v = 0; v != Math.min(size, 5); ++v) itemAt(v).plan(el);
  }

  @Override
  public final String toErrorString() {
    return toString(true);
  }

  @Override
  public String toString() {
    return toString(false);
  }

  /**
   * Returns a string representation of the sequence.
   * @param error error flag
   * @return string
   */
  private String toString(final boolean error) {
    final StringBuilder sb = new StringBuilder(PAR1);
    for(int i = 0; i < size; ++i) {
      sb.append(i == 0 ? "" : SEP);
      final Item it = itemAt(i);
      sb.append(error ? it.toErrorString() : it.toString());
      if(sb.length() <= 16 || i + 1 == size) continue;
      // output is chopped to prevent too long error strings
      sb.append(SEP).append(DOTS);
      break;
    }
    return sb.append(PAR2).toString();
  }
}
