package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Seq extends Value {
  /** Length. */
  final long size;

  /**
   * Constructor.
   * @param s size
   */
  Seq(final long s) {
    this(s, AtomType.SEQ);
  }

  /**
   * Constructor, specifying a type.
   * @param s size
   * @param t type
   */
  Seq(final long s, final Type t) {
    super(t);
    size = s;
  }

  /**
   * Returns a value representation of the specified items.
   * @param v value
   * @param s size
   * @return resulting item or sequence
   */
  public static Value get(final Item[] v, final int s) {
    return s == 0 ? Empty.SEQ : s == 1 ? v[0] : new ItemSeq(v, s);
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
  public final Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    throw XPSEQ.thrw(ii, this);
  }

  @Override
  public final Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return ebv(ctx, ii);
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
  public void plan(final FElem plan) {
    final FElem el = planElem(SIZE, size);
    addPlan(plan, el);
    for(int v = 0; v != Math.min(size, 5); ++v) itemAt(v).plan(el);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(PAR1);
    for(int i = 0; i < size; ++i) {
      sb.append(i != 0 ? SEP : "").append(itemAt(i));
      if(sb.length() <= 32 || i + 1 == size) continue;
      // output is chopped to prevent too long error strings
      sb.append(SEP + DOTS);
      break;
    }
    return sb.append(PAR2).toString();
  }
}
