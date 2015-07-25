package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class Seq extends Value {
  /** Length. */
  protected final long size;

  /**
   * Constructor.
   * @param size size
   */
  protected Seq(final long size) {
    this(size, AtomType.ITEM);
  }

  /**
   * Constructor, specifying a type.
   * @param size size
   * @param type type
   */
  protected Seq(final long size, final Type type) {
    super(type);
    this.size = size;
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
  public final boolean isEmpty() {
    return false;
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
  public ValueIter iter() {
    return new ValueIter() {
      int c;
      @Override
      public Item get(final long i) { return itemAt(i); }
      @Override
      public Item next() { return c < size ? itemAt(c++) : null; }
      @Override
      public long size() { return size; }
      @Override
      public Value value() { return Seq.this; }
    };
  }

  @Override
  public Value subSeq(final long start, final long len) {
    return len == 0   ? Empty.SEQ
         : len == 1   ? itemAt(start)
         : len < size ? new SubSeq(this, start, len)
                      : this;
  }

  /**
   * Inserts a value at the given position into this sequence and returns the resulting sequence.
   * @param pos position at which the value should be inserted, must be between 0 and {@link #size}
   * @param val value to insert
   * @return resulting value
   */
  public Value insertBefore(final long pos, final Value val) {
    final long n = val.size();
    return n == 1 ? insert(pos, (Item) val) : copyInsert(pos, val);
  }

  /**
   * Inserts an item at the given position into this sequence and returns the resulting sequence.
   * @param pos position at which the item should be inserted, must be between 0 and {@link #size}
   * @param val value to insert
   * @return resulting value
   */
  public abstract Value insert(final long pos, final Item val);

  /**
   * Helper for {@link #insertBefore(long, Value)} that copies all items into a {@link TreeSeq}.
   * @param pos position at which the value should be inserted, must be between 0 and {@link #size}
   * @param val value to insert
   * @return resulting value
   */
  final Value copyInsert(final long pos, final Value val) {
    if(val.isEmpty()) return this;
    final ValueBuilder vb = new ValueBuilder();
    for(long i = 0; i < pos; i++) vb.add(itemAt(i));
    vb.add(val);
    for(long i = pos; i < size; i++) vb.add(itemAt(i));
    return vb.value(type);
  }

  /**
   * Removes the item at the given position in this sequence and returns the resulting sequence.
   * @param pos position of the item to remove, must be between 0 and {@link #size} - 1
   * @return resulting sequence
   */
  public abstract Value remove(final long pos);

  /**
   * Helper for {@link #remove(long)} that copies all items into a {@link TreeSeq}.
   * @param pos position of the item to remove, must be between 0 and {@link #size} - 1
   * @return resulting sequence
   */
  final Value copyRemove(final long pos) {
    final ValueBuilder vb = new ValueBuilder();
    for(long i = 0; i < pos; i++) vb.add(itemAt(i));
    for(long i = pos + 1; i < size; i++) vb.add(itemAt(i));
    return vb.value(type);
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
    final StringBuilder sb = new StringBuilder(PAREN1);
    for(int i = 0; i < size; ++i) {
      sb.append(i == 0 ? "" : SEP);
      final Item it = itemAt(i);
      sb.append(error ? it.toErrorString() : it.toString());
      if(sb.length() <= 16 || i + 1 == size) continue;
      // output is chopped to prevent too long error strings
      sb.append(SEP).append(DOTS);
      break;
    }
    return sb.append(PAREN2).toString();
  }
}
