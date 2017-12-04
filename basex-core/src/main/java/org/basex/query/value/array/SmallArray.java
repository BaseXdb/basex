package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * A small array that is represented in a single Java array.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
final class SmallArray extends Array {
  /** The elements. */
  final Value[] elems;

  /**
   * Constructor.
   * @param elems elements
   */
  SmallArray(final Value[] elems) {
    this.elems = elems;
    assert elems.length >= 1 && elems.length <= MAX_SMALL;
  }

  @Override
  public Array cons(final Value head) {
    if(elems.length < MAX_SMALL) {
      final Value[] newElems = slice(elems, -1, elems.length);
      newElems[0] = head;
      return new SmallArray(newElems);
    }

    final int mid = MIN_DIGIT - 1;
    final Value[] left = slice(elems, -1, mid), right = slice(elems, mid, elems.length);
    left[0] = head;
    return new BigArray(left, right);
  }

  @Override
  public Array snoc(final Value last) {
    if(elems.length < MAX_SMALL) {
      final Value[] newElems = slice(elems, 0, elems.length + 1);
      newElems[newElems.length - 1] = last;
      return new SmallArray(newElems);
    }

    final Value[] left = slice(elems, 0, MIN_DIGIT),
        right = slice(elems, MIN_DIGIT, elems.length + 1);
    right[right.length - 1] = last;
    return new BigArray(left, right);
  }

  @Override
  public Value get(final long index) {
    return elems[(int) index];
  }

  @Override
  public Array put(final long pos, final Value val) {
    final Value[] values = elems.clone();
    values[(int) pos] = val;
    return new SmallArray(values);
  }

  @Override
  public long arraySize() {
    return elems.length;
  }

  @Override
  public Array concat(final Array seq) {
    return seq.isEmptyArray() ? this : seq.consSmall(elems);
  }

  @Override
  public Value head() {
    return elems[0];
  }

  @Override
  public Value last() {
    return elems[elems.length - 1];
  }

  @Override
  public Array init() {
    if(elems.length == 1) return empty();
    return new SmallArray(slice(elems, 0, elems.length - 1));
  }

  @Override
  public Array tail() {
    if(elems.length == 1) return empty();
    return new SmallArray(slice(elems, 1, elems.length));
  }

  @Override
  public boolean isEmptyArray() {
    return false;
  }

  @Override
  public Array reverseArray(final QueryContext qc) {
    qc.checkStop();
    final int n = elems.length;
    if(n == 1) return this;
    final Value[] es = new Value[n];
    for(int i = 0; i < n; i++) es[i] = elems[n - 1 - i];
    return new SmallArray(es);
  }

  @Override
  public Array insertBefore(final long pos, final Value val, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = elems.length;
    final Value[] out = new Value[n + 1];
    System.arraycopy(elems, 0, out, 0, p);
    out[p] = val;
    System.arraycopy(elems, p, out, p + 1, n - p);

    if(n < MAX_SMALL) return new SmallArray(out);
    return new BigArray(slice(out, 0, MIN_DIGIT), slice(out, MIN_DIGIT, n + 1));
  }

  @Override
  public Array remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = elems.length;
    if(n == 1) return empty();

    final Value[] out = new Value[n - 1];
    System.arraycopy(elems, 0, out, 0, p);
    System.arraycopy(elems, p + 1, out, p, n - 1 - p);
    return new SmallArray(out);
  }

  @Override
  public Array subArray(final long pos, final long len, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = (int) len;
    return n == 0 ? Array.empty() : new SmallArray(slice(elems, p, p + n));
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    return new ListIterator<Value>() {
      private int index = (int) Math.max(0, Math.min(start, elems.length));

      @Override
      public int nextIndex() {
        return index;
      }

      @Override
      public boolean hasNext() {
        return index < elems.length;
      }

      @Override
      public Value next() {
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
      public Value previous() {
        return elems[--index];
      }

      @Override
      public void set(final Value e) {
        throw Util.notExpected();
      }

      @Override
      public void add(final Value e) {
        throw Util.notExpected();
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  @Override
  void checkInvariants() {
    final int n = elems.length;
    if(n == 0) throw new AssertionError("Empty array in " + Util.className(this));
    if(n > MAX_SMALL) throw new AssertionError("Array too big: " + n);
  }

  @Override
  Array consSmall(final Value[] left) {
    final int l = left.length, r = elems.length, n = l + r;
    if(Math.min(l, r) >= MIN_DIGIT) {
      // both arrays can be used as digits
      return new BigArray(left, elems);
    }

    final Value[] out = new Value[n];
    System.arraycopy(left, 0, out, 0, l);
    System.arraycopy(elems, 0, out, l, r);
    if(n <= MAX_SMALL) return new SmallArray(out);

    final int mid = n / 2;
    return new BigArray(slice(out, 0, mid), slice(out, mid, n));
  }
}
