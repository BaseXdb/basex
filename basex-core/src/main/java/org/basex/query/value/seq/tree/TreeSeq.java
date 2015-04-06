package org.basex.query.value.seq.tree;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * An array storing {@link Item}s.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public abstract class TreeSeq extends Seq {
  /** Minimum size of a leaf. */
  static final int MIN_LEAF = 8;
  /** Maximum size of a leaf. */
  static final int MAX_LEAF = 2 * MIN_LEAF - 1;
  /** Minimum number of elements in a digit. */
  static final int MIN_DIGIT = MIN_LEAF / 2;
  /** Maximum number of elements in a digit. */
  static final int MAX_DIGIT = MAX_LEAF + MIN_DIGIT;
  /** Maximum size of a small array. */
  static final int MAX_SMALL = 2 * MIN_DIGIT - 1;

  /** Item Types. */
  Type ret;

  /**
   * Default constructor.
   * @param size number of elements in this sequence
   */
  TreeSeq(final long size) {
    super(size);
  }

  /**
   * Default constructor.
   * @param size number of elements in this sequence
   * @param ret type of all items in this sequence
   */
  TreeSeq(final long size, final Type ret) {
    super(size, ret == null ? AtomType.ITEM : ret);
    this.ret = ret;
  }

  @Override
  public final Value insertBefore(final long pos, final Value val) {
    final long n = val.size();
    if(n < 2) return n == 0 ? this : insert(pos, (Item) val);

    final long l = pos, r = size - pos;
    if(val instanceof TreeSeq && (l == 0 || r == 0)) {
      final TreeSeq other = (TreeSeq) val;
      return l == 0 ? other.concat(this) : concat(other);
    }

    final ValueBuilder vb = new ValueBuilder();
    if(l < MAX_SMALL) {
      vb.add(val);
      for(long i = l; --i >= 0;) vb.addFront(itemAt(i));
    } else {
      vb.add(subSeq(0, l));
      vb.add(val);
    }

    if(r < MAX_SMALL) {
      for(long i = size - r; i < size; i++) vb.add(itemAt(i));
    } else {
      vb.add(subSeq(pos, r));
    }

    return vb.value();
  }

  /**
   * Concatenates this sequence with another one.
   * Running time: <i>O(log (min { this.size(), other.size() }))</i>
   * @param other array to append to the end of this array
   * @return resulting array
   */
  public abstract TreeSeq concat(final TreeSeq other);

  /**
   * Iterator over the members of this array.
   * @param start starting position
   *   (i.e. the position initially returned by {@link ListIterator#nextIndex()})
   * @return array over the array members
   */
  public abstract ListIterator<Item> members(final long start);

  /**
   * Iterator over the members of this array.
   * @return array over the array members
   */
  public final ListIterator<Item> members() {
    return members(0);
  }

  @Override
  public Object toJava() throws QueryException {
    final Object[] obj = new Object[(int) size];
    for(int s = 0; s < size; s++) obj[s] = itemAt(s).toJava();
    return obj;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(SIZE, size);
    addPlan(plan, el);
    for(int v = 0; v != Math.min(size, 5); ++v) itemAt(v).plan(el);
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

  @Override
  public ValueIter iter() {
    return new ValueIter() {
      private final Iterator<Item> members = members();

      @Override
      public Item next() {
        return members.hasNext() ? members.next() : null;
      }

      @Override
      public Item get(final long i) {
        return TreeSeq.this.itemAt(i);
      }

      @Override
      public long size() {
        return TreeSeq.this.size();
      }

      @Override
      public Value value() {
        return TreeSeq.this;
      }
    };
  }

  @Override
  public Value materialize(final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final Iterator<Item> iter = members();
    while(iter.hasNext()) vb.add(iter.next().materialize(ii));
    return vb.value();
  }

  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final Iterator<Item> iter = members();
    while(iter.hasNext()) vb.add(iter.next().atomValue(ii));
    return vb.value();
  }

  @Override
  public long atomSize() {
    long s = 0;
    final Iterator<Item> iter = members();
    while(iter.hasNext()) s += iter.next().atomSize();
    return s;
  }

  @Override
  public int writeTo(final Item[] arr, final int off) {
    final int n = (int) Math.min(arr.length - off, size());
    final Iterator<Item> iter = members();
    for(int i = 0; i < n; i++) arr[off + i] = iter.next();
    return n;
  }

  @Override
  public boolean homogeneous() {
    return ret != null && ret != AtomType.ITEM;
  }

  @Override
  public boolean iterable() {
    return false;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item head = itemAt(0);
    if(head instanceof ANode) return head;
    throw EBV_X.get(ii, this);
  }

  @Override
  public SeqType seqType() {
    if(ret == null) {
      final Iterator<Item> iter = members();
      Type t = iter.next().type;
      while(iter.hasNext()) {
        if(t != iter.next().type) {
          t = AtomType.ITEM;
          break;
        }
      }
      ret = t;
      type = t;
    }
    return SeqType.get(ret, Occ.ONE_MORE);
  }

  /**
   * Prepends the given elements to this array.
   * @param vals values, with length at most {@link TreeSeq#MAX_SMALL}
   * @return resulting array
   */
  abstract TreeSeq consSmall(final Item[] vals);

  /**
   * Returns an array containing the values at the indices {@code from} to {@code to - 1} in
   * the given array. Its length is always {@code to - from}. If {@code from} is smaller than zero,
   * the first {@code -from} entries in the resulting array are {@code null}.
   * If {@code to > arr.length} then the last {@code to - arr.length} entries are {@code null}.
   * If {@code from == 0 && to == arr.length}, the original array is returned.
   * @param arr input array
   * @param from first index, inclusive (may be negative)
   * @param to last index, exclusive (may be greater than {@code arr.length})
   * @return resulting array
   */
  static final Item[] slice(final Item[] arr, final int from, final int to) {
    final Item[] out = new Item[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, arr.length);
    final int out0 = Math.max(-from, 0);
    System.arraycopy(arr, in0, out, out0, in1 - in0);
    return out;
  }

  /**
   * Concatenates the two int arrays.
   * @param as first array
   * @param bs second array
   * @return resulting array
   */
  static final Item[] concat(final Item[] as, final Item[] bs) {
    final int l = as.length, r = bs.length, n = l + r;
    final Item[] out = new Item[n];
    System.arraycopy(as, 0, out, 0, l);
    System.arraycopy(bs, 0, out, l, r);
    return out;
  }

  /**
   * Checks that this array's implementation does not violate any invariants.
   * @throws AssertionError if an invariant was violated
   */
  abstract void checkInvariants();
}
