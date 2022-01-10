package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A small array that is stored in a single Java array.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
final class SmallArray extends XQArray {
  /** The members. */
  final Value[] members;

  /**
   * Constructor.
   * @param members members
   * @param type type
   */
  SmallArray(final Value[] members, final Type type) {
    super(type);
    this.members = members;
    assert members.length >= 1 && members.length <= MAX_SMALL;
  }

  @Override
  public XQArray cons(final Value head) {
    final Type tp = union(head);
    if(members.length < MAX_SMALL) {
      final Value[] newMembers = slice(members, -1, members.length);
      newMembers[0] = head;
      return new SmallArray(newMembers, tp);
    }

    final int mid = MIN_DIGIT - 1;
    final Value[] left = slice(members, -1, mid), right = slice(members, mid, members.length);
    left[0] = head;
    return new BigArray(left, right, tp);
  }

  @Override
  public XQArray snoc(final Value last) {
    final Type tp = union(last);
    if(members.length < MAX_SMALL) {
      final Value[] newMembers = slice(members, 0, members.length + 1);
      newMembers[newMembers.length - 1] = last;
      return new SmallArray(newMembers, tp);
    }

    final Value[] left = slice(members, 0, MIN_DIGIT),
        right = slice(members, MIN_DIGIT, members.length + 1);
    right[right.length - 1] = last;
    return new BigArray(left, right, tp);
  }

  @Override
  public Value get(final long index) {
    return members[(int) index];
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    final Value[] values = members.clone();
    values[(int) pos] = value;
    return new SmallArray(values, union(value));
  }

  @Override
  public long arraySize() {
    return members.length;
  }

  @Override
  public XQArray concat(final XQArray seq) {
    return seq.isEmptyArray() ? this : seq.prepend(this);
  }

  @Override
  public Value head() {
    return members[0];
  }

  @Override
  public Value last() {
    return members[members.length - 1];
  }

  @Override
  public XQArray init() {
    if(members.length == 1) return empty();
    return new SmallArray(slice(members, 0, members.length - 1), type);
  }

  @Override
  public XQArray tail() {
    if(members.length == 1) return empty();
    return new SmallArray(slice(members, 1, members.length), type);
  }

  @Override
  public boolean isEmptyArray() {
    return false;
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    qc.checkStop();
    final int n = members.length;
    if(n == 1) return this;
    final Value[] values = new Value[n];
    for(int i = 0; i < n; i++) values[i] = members[n - 1 - i];
    return new SmallArray(values, type);
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    final Type tp = union(value);
    qc.checkStop();
    final int p = (int) pos, n = members.length;
    final Value[] out = new Value[n + 1];
    Array.copy(members, p, out);
    out[p] = value;
    Array.copy(members, p, n - p, out, p + 1);

    if(n < MAX_SMALL) return new SmallArray(out, tp);
    return new BigArray(slice(out, 0, MIN_DIGIT), slice(out, MIN_DIGIT, n + 1), tp);
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = members.length;
    if(n == 1) return empty();

    final Value[] out = new Value[n - 1];
    Array.copy(members, p, out);
    Array.copy(members, p + 1, n - 1 - p, out, p);
    return new SmallArray(out, type);
  }

  @Override
  public XQArray subArray(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = (int) length;
    return n == 0 ? XQArray.empty() : new SmallArray(slice(members, p, p + n), type);
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    return new ListIterator<Value>() {
      private int index = (int) start;

      @Override
      public int nextIndex() {
        return index;
      }

      @Override
      public boolean hasNext() {
        return index < members.length;
      }

      @Override
      public Value next() {
        return members[index++];
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
        return members[--index];
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
    final int n = members.length;
    if(n == 0) throw new AssertionError("Empty array in " + Util.className(this));
    if(n > MAX_SMALL) throw new AssertionError("Array too big: " + n);
  }

  @Override
  XQArray prepend(final SmallArray array) {
    final Type tp = type.union(array.type);
    final Value[] values = array.members;
    final int a = values.length, b = members.length, n = a + b;

    // both arrays can be used as digits
    if(Math.min(a, b) >= MIN_DIGIT) return new BigArray(values, members, tp);

    final Value[] out = new Value[n];
    Array.copy(values, a, out);
    Array.copyFromStart(members, b, out, a);
    if(n <= MAX_SMALL) return new SmallArray(out, tp);

    final int mid = n / 2;
    return new BigArray(slice(out, 0, mid), slice(out, mid, n), tp);
  }
}
