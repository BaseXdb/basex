package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * A small array that is represented in a single Java array.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
final class SmallArray extends XQArray {
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
  public XQArray cons(final Value head) {
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
  public XQArray snoc(final Value last) {
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
  public XQArray put(final long pos, final Value val) {
    final Value[] values = elems.clone();
    values[(int) pos] = val;
    return new SmallArray(values);
  }

  @Override
  public long arraySize() {
    return elems.length;
  }

  @Override
  public XQArray concat(final XQArray seq) {
    return seq.isEmptyArray() ? this : seq.prepend(this);
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
  public XQArray init() {
    if(elems.length == 1) return empty();
    return new SmallArray(slice(elems, 0, elems.length - 1));
  }

  @Override
  public XQArray tail() {
    if(elems.length == 1) return empty();
    return new SmallArray(slice(elems, 1, elems.length));
  }

  @Override
  public boolean isEmptyArray() {
    return false;
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    qc.checkStop();
    final int n = elems.length;
    if(n == 1) return this;
    final Value[] es = new Value[n];
    for(int i = 0; i < n; i++) es[i] = elems[n - 1 - i];
    return new SmallArray(es);
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = elems.length;
    final Value[] out = new Value[n + 1];
    Array.copy(elems, p, out);
    out[p] = value;
    Array.copy(elems, p, n - p, out, p + 1);

    if(n < MAX_SMALL) return new SmallArray(out);
    return new BigArray(slice(out, 0, MIN_DIGIT), slice(out, MIN_DIGIT, n + 1));
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = elems.length;
    if(n == 1) return empty();

    final Value[] out = new Value[n - 1];
    Array.copy(elems, p, out);
    Array.copy(elems, p + 1, n - 1 - p, out, p);
    return new SmallArray(out);
  }

  @Override
  public XQArray subArray(final long pos, final long len, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = (int) len;
    return n == 0 ? XQArray.empty() : new SmallArray(slice(elems, p, p + n));
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
  XQArray prepend(final SmallArray array) {
    final Value[] values = array.elems;
    final int a = values.length, b = elems.length, n = a + b;

    // both arrays can be used as digits
    if(Math.min(a, b) >= MIN_DIGIT) return new BigArray(values, elems);

    final Value[] out = new Value[n];
    Array.copy(values, a, out);
    Array.copyFromStart(elems, b, out, a);
    if(n <= MAX_SMALL) return new SmallArray(out);

    final int mid = n / 2;
    return new BigArray(slice(out, 0, mid), slice(out, mid, n));
  }
}
