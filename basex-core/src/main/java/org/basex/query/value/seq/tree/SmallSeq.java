package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * A small sequence that is represented as a single Java array.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
final class SmallSeq extends TreeSeq {
  /** The elements. */
  final Item[] elems;

  /**
   * Constructor.
   * @param elems elements
   * @param type type of all elements in this sequence
   */
  SmallSeq(final Item[] elems, final Type type) {
    super(elems.length, type);
    this.elems = elems;
    assert elems.length >= 2 && elems.length <= MAX_SMALL;
  }

  @Override
  public Item itemAt(final long index) {
    // index to small?
    if(index < 0) throw new IndexOutOfBoundsException("Index < 0: " + index);

    // index too big?
    if(index >= elems.length) throw new IndexOutOfBoundsException(index + " >= " + elems.length);

    return elems[(int) index];
  }

  @Override
  public TreeSeq reverse() {
    final int n = elems.length;
    final Item[] es = new Item[n];
    for(int i = 0; i < n; i++) es[i] = elems[n - 1 - i];
    return new SmallSeq(es, type);
  }

  @Override
  public TreeSeq insert(final long pos, final Item val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > elems.length) throw new IndexOutOfBoundsException("position too big: " + pos);

    final int p = (int) pos, n = elems.length;
    final Item[] out = new Item[n + 1];
    System.arraycopy(elems, 0, out, 0, p);
    out[p] = val;
    System.arraycopy(elems, p, out, p + 1, n - p);

    if(n < MAX_SMALL) return new SmallSeq(out, null);
    return new BigSeq(slice(out, 0, MIN_DIGIT), FingerTree.empty(),
        slice(out, MIN_DIGIT, n + 1), null);
  }

  @Override
  public Value remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos >= elems.length) throw new IndexOutOfBoundsException("position too big: " + pos);
    final int p = (int) pos, n = elems.length;
    if(n == 2) return elems[pos == 0 ? 1 : 0];

    final Item[] out = new Item[n - 1];
    System.arraycopy(elems, 0, out, 0, p);
    System.arraycopy(elems, p + 1, out, p, n - 1 - p);
    return new SmallSeq(out, type);
  }

  @Override
  public Value subSeq(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    if(pos + len > elems.length) throw new IndexOutOfBoundsException("end out of bounds: " +
        (pos + len) + " > " + elems.length);

    final int p = (int) pos, l = (int) len;
    return l == 0 ? Empty.SEQ : l == 1 ? elems[p] : l == size ? this :
      new SmallSeq(slice(elems, p, p + l), type);
  }

  @Override
  public TreeSeq concat(final TreeSeq other) {
    return other.consSmall(elems);
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
        return index < elems.length;
      }

      @Override
      public Item next() {
        if(index >= elems.length) throw new NoSuchElementException();
        return elems[index++];
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
        if(index <= 0) throw new NoSuchElementException();
        return elems[--index];
      }

      @Override
      public void set(final Item e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void add(final Item e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public BasicIter<Item> iter() {
    return new BasicIter<Item>(size) {
      @Override
      public Item get(final long i) {
        return elems[(int) i];
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
    final int l = left.length, r = elems.length, n = l + r;
    if(Math.min(l, r) >= MIN_DIGIT) {
      // both arrays can be used as digits
      return new BigSeq(left, FingerTree.empty(), elems, null);
    }

    final Item[] out = new Item[n];
    System.arraycopy(left, 0, out, 0, l);
    System.arraycopy(elems, 0, out, l, r);
    if(n <= MAX_SMALL) return new SmallSeq(out, null);

    final int mid = n / 2;
    return new BigSeq(slice(out, 0, mid), FingerTree.empty(), slice(out, mid, n), null);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof SmallSeq ? Arrays.equals(elems, ((SmallSeq) obj).elems) :
      super.equals(obj));
  }
}
