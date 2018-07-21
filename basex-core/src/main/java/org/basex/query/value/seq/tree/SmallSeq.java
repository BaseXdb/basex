package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A small sequence that is represented as a single Java array.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
final class SmallSeq extends TreeSeq {
  /** The elements. */
  final Item[] items;

  /**
   * Constructor.
   * @param items elements
   * @param type type of all items in this sequence, can be {@code null}
   */
  SmallSeq(final Item[] items, final Type type) {
    super(items.length, type);
    this.items = items;
    assert items.length >= 2 && items.length <= MAX_SMALL;
  }

  @Override
  public Item itemAt(final long index) {
    return items[(int) index];
  }

  @Override
  public TreeSeq reverse(final QueryContext qc) {
    final int el = items.length;
    final Item[] es = new Item[el];
    for(int e = 0; e < el; e++) es[e] = items[el - 1 - e];
    return new SmallSeq(es, type);
  }

  @Override
  public TreeSeq insert(final long pos, final Item value, final QueryContext qc) {
    final int p = (int) pos, n = items.length;
    final Item[] out = new Item[n + 1];
    System.arraycopy(items, 0, out, 0, p);
    out[p] = value;
    System.arraycopy(items, p, out, p + 1, n - p);

    if(n < MAX_SMALL) return new SmallSeq(out, null);
    return new BigSeq(slice(out, 0, MIN_DIGIT), FingerTree.empty(),
        slice(out, MIN_DIGIT, n + 1), null);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    final int p = (int) pos, n = items.length;
    if(n == 2) return items[pos == 0 ? 1 : 0];

    final Item[] out = new Item[n - 1];
    System.arraycopy(items, 0, out, 0, p);
    System.arraycopy(items, p + 1, out, p, n - 1 - p);
    return new SmallSeq(out, type);
  }

  @Override
  protected Seq subSeq(final long offset, final long length, final QueryContext qc) {
    final int o = (int) offset, l = (int) length;
    return new SmallSeq(slice(items, o, o + l), type);
  }

  @Override
  public TreeSeq concat(final TreeSeq other) {
    return other.consSmall(items);
  }

  @Override
  public ListIterator<Item> iterator(final long start) {
    return new ListIterator<Item>() {
      private int index = (int) start;

      @Override
      public int nextIndex() {
        return index;
      }

      @Override
      public boolean hasNext() {
        return index < items.length;
      }

      @Override
      public Item next() {
        return items[index++];
      }

      @Override
      public int previousIndex() {
        return index - 1;
      }

      @Override
      public boolean hasPrevious() {
        return index > 0;
      }

      @Override
      public Item previous() {
        return items[--index];
      }

      @Override
      public void set(final Item e) {
        throw Util.notExpected();
      }

      @Override
      public void add(final Item e) {
        throw Util.notExpected();
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  @Override
  public BasicIter<Item> iter() {
    return new BasicIter<Item>(size) {
      @Override
      public Item get(final long i) {
        return items[(int) i];
      }

      @Override
      public Value value() {
        return SmallSeq.this;
      }

      @Override
      public Value value(final QueryContext qc) {
        return value();
      }
    };
  }

  @Override
  TreeSeq consSmall(final Item[] left) {
    final int l = left.length, r = items.length, n = l + r;
    if(Math.min(l, r) >= MIN_DIGIT) {
      // both arrays can be used as digits
      return new BigSeq(left, FingerTree.empty(), items, null);
    }

    final Item[] out = new Item[n];
    System.arraycopy(left, 0, out, 0, l);
    System.arraycopy(items, 0, out, l, r);
    if(n <= MAX_SMALL) return new SmallSeq(out, null);

    final int mid = n / 2;
    return new BigSeq(slice(out, 0, mid), FingerTree.empty(), slice(out, mid, n), null);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof SmallSeq ? Arrays.equals(items, ((SmallSeq) obj).items) :
      super.equals(obj));
  }
}
