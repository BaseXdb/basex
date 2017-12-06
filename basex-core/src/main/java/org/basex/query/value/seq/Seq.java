package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Seq extends Value {
  /** Indicates if all items have exactly the same type. */
  public boolean homo;
  /** Length. */
  protected long size;

  /**
   * Constructor, specifying a type.
   * @param size size
   * @param type type
   */
  protected Seq(final long size, final Type type) {
    super(type);
    this.size = size;
    homo = type != AtomType.ITEM;
  }

  @Override
  public Object toJava() throws QueryException {
    final ArrayList<Object> obj = new ArrayList<>((int) size);
    for(final Item item : this) obj.add(item.toJava());
    return obj.toArray();
  }

  @Override
  public final long size() {
    return size;
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo info) throws QueryException {
    throw SEQFOUND_X.get(info, this);
  }

  @Override
  public final Item test(final QueryContext qc, final InputInfo info) throws QueryException {
    return ebv(qc, info);
  }

  @Override
  public BasicIter<Item> iter() {
    return new BasicIter<Item>(size) {
      @Override
      public Item get(final long i) {
        return itemAt(i);
      }
      @Override
      public Value value() {
        return Seq.this;
      }
      @Override
      public Value value(final QueryContext qc) {
        return value();
      }
    };
  }

  @Override
  public final Value subSequence(final long start, final long length, final QueryContext qc) {
    return length == 0 ? Empty.SEQ :
           length == 1 ? itemAt(start) :
           length == size() ? this :
           subSeq(start, length, qc);
  }

  /**
   * Returns a sub sequence of this value with the given start and length.
   * @param offset offset (>= 0)
   * @param length number of items (1 < length < size())
   * @param qc query context
   * @return sub sequence
   */
  protected Seq subSeq(final long offset, final long length, final QueryContext qc) {
    qc.checkStop();
    return new SubSeq(this, offset, length);
  }

  /**
   * Inserts a value at the given position into this sequence and returns the resulting sequence.
   * @param pos position at which the value should be inserted, must be between 0 and {@link #size}
   * @param val value to insert
   * @param qc query context
   * @return resulting value
   */
  public Value insertBefore(final long pos, final Value val, final QueryContext qc) {
    final long n = val.size();
    return n == 1 ? insert(pos, (Item) val, qc) : n == 0 ? this : copyInsert(pos, val, qc);
  }

  /**
   * Inserts an item at the given position into this sequence and returns the resulting sequence.
   * @param pos position at which the item should be inserted, must be between 0 and {@link #size}
   * @param val value to insert
   * @param qc query context
   * @return resulting value
   */
  public abstract Value insert(long pos, Item val, QueryContext qc);

  /**
   * Helper for {@link #insertBefore(long, Value, QueryContext)} that copies all items into a
   * {@link TreeSeq}.
   * @param pos position at which the value should be inserted, must be between 0 and {@link #size}
   * @param val value to insert
   * @param qc query context
   * @return resulting value
   */
  final Value copyInsert(final long pos, final Value val, final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    if(pos == size) {
      vb.add(this, val);
    } else {
      for(long i = 0; i < pos; i++) vb.add(itemAt(i));
      vb.add(val);
      for(long i = pos; i < size; i++) vb.add(itemAt(i));
    }
    return vb.value(type);
  }

  /**
   * Removes the item at the given position in this sequence and returns the resulting sequence.
   * @param pos position of the item to remove, must be between 0 and {@link #size} - 1
   * @param qc query context
   * @return resulting sequence
   */
  public abstract Value remove(long pos, QueryContext qc);

  /**
   * Helper for {@link #remove(long, QueryContext)} that copies all items into a {@link TreeSeq}.
   * @param pos position of the item to remove, must be between 0 and {@link #size} - 1
   * @param qc query context
   * @return resulting sequence
   */
  final Value copyRemove(final long pos, final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < pos; i++) vb.add(itemAt(i));
    for(long i = pos + 1; i < size; i++) vb.add(itemAt(i));
    return vb.value(type);
  }

  @Override
  public final int hash(final InputInfo info) throws QueryException {
    // final hash function because equivalent sequences *must* produce the
    // same hash value, otherwise they get lost in hash maps.
    // example: hash(RangeSeq(1 to 3)) == hash(ItrSeq(1, 2, 3))
    //                                 == hash(ItemSeq(Itr(1), Itr(2), Itr(3)))
    int h = 1;
    for(long v = Math.min(size, 5); --v >= 0;) h = 31 * h + itemAt(v).hash(info);
    return h;
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo info) throws QueryException {
    throw SEQFOUND_X.get(info, this);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Seq)) return false;
    final Seq s = (Seq) obj;
    if(size != s.size) return false;
    final BasicIter<Item> iter1 = iter(), iter2 = s.iter();
    for(Item item1; (item1 = iter1.next()) != null;) {
      if(!item1.equals(iter2.next())) return false;
    }
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem(SIZE, size, TYPE, seqType());
    addPlan(plan, elem);
    for(long i = 0; i < size; i++) {
      if(i == 3 && i + 1 < size) {
        elem.add(new FElem("etc"));
        break;
      }
      itemAt(i).plan(elem);
    }
  }

  @Override
  public final SeqType seqType() {
    return SeqType.get(type, Occ.ONE_MORE);
  }

  @Override
  public String description() {
    return type + " " + SEQUENCE;
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
      final Item item = itemAt(i);
      sb.append(error ? item.toErrorString() : item.toString());
      if(sb.length() <= 16 || i + 1 == size) continue;
      // output is chopped to prevent too long error strings
      sb.append(SEP).append(DOTS);
      break;
    }
    return sb.append(PAREN2).toString();
  }
}
