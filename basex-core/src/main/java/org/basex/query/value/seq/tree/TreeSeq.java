package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A tree storing {@link Item}s.
 *
 * @author BaseX Team, BSD License
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

  /**
   * Default constructor.
   * @param size number of items in this sequence
   * @param type type of all items in this sequence
   */
  TreeSeq(final long size, final Type type) {
    super(size, type);
  }

  /**
   * Helper for {@link #insert(long, Value, Job)} that copies all items into a
   * {@link TreeSeq}.
   * @param pos position at which the value should be inserted, must be between 0 and {@link #size}
   * @param value value to insert
   * @param job interruptible job
   * @return resulting value
   */
  protected final Value copyInsert(final long pos, final Value value, final Job job) {
    final long right = size - pos;
    if(value instanceof final TreeSeq other && (pos == 0 || right == 0)) {
      return pos == 0 ? other.concat(this) : concat(other);
    }

    final TreeSeqBuilder sb = new TreeSeqBuilder();
    if(pos < MAX_SMALL) {
      sb.add(value, job);
      for(long i = pos; --i >= 0;) sb.prepend(itemAt(i));
    } else {
      sb.add(subsequence(0, pos, job), job);
      sb.add(value, job);
    }

    if(right < MAX_SMALL) {
      for(long i = size - right; i < size; i++) sb.add(itemAt(i));
    } else {
      sb.add(subsequence(pos, right, job), job);
    }

    return sb.value(type.union(value.type));
  }

  /**
   * Iterator over the members of this sequence.
   * @param start starting position
   *   (i.e. the position initially returned by {@link ListIterator#nextIndex()})
   * @return array over the array members
   */
  public abstract ListIterator<Item> iterator(long start);

  @Override
  public final Iterator<Item> iterator() {
    return iterator(0);
  }

  @Override
  public abstract BasicIter<Item> iter();

  /**
   * Concatenates this sequence with another one.
   * @param other array to append to the end of this array
   * @return resulting array
   */
  abstract TreeSeq concat(TreeSeq other);

  @Override
  public final Value shrink(final Job job) throws QueryException {
    return rebuild(job);
  }

  /**
   * Prepends the given sequence to this sequence.
   * @param seq small sequence
   * @return resulting sequence
   */
  abstract TreeSeq prepend(SmallSeq seq);

  /**
   * Returns items containing the values at the indices {@code from} to {@code to - 1} in
   * the given sequence. Its length is always {@code to - from}. If {@code from} is smaller than
   * zero, the first {@code -from} entries in the resulting sequence are {@code null}.
   * If {@code to > arr.length} then the last {@code to - arr.length} entries are {@code null}.
   * If {@code from == 0 && to == arr.length}, the original items are returned.
   * @param items input sequence
   * @param from first index, inclusive (can be negative)
   * @param to last index, exclusive (can be greater than length of input sequence)
   * @return resulting items
   */
  static Item[] slice(final Item[] items, final int from, final int to) {
    final Item[] out = new Item[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, items.length);
    final int out0 = Math.max(-from, 0);
    Array.copy(items, in0, in1 - in0, out, out0);
    return out;
  }

  /**
   * Concatenates the two item arrays.
   * @param as first array
   * @param bs second array
   * @return resulting array
   */
  static Item[] concat(final Item[] as, final Item[] bs) {
    final int l = as.length, r = bs.length, n = l + r;
    final Item[] out = new Item[n];
    Array.copy(as, l, out);
    Array.copyFromStart(bs, r, out, l);
    return out;
  }
}
