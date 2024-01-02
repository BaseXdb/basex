package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A small array that is stored in a single Java array.
 *
 * @author BaseX Team 2005-24, BSD License
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
    assert members.length >= 2 && members.length <= MAX_SMALL;
  }

  @Override
  public XQArray prepend(final Value head) {
    final Type tp = union(head);
    final int ml = members.length;
    if(ml < MAX_SMALL) {
      final Value[] newMembers = slice(members, -1, ml);
      newMembers[0] = head;
      return new SmallArray(newMembers, tp);
    }

    final int mid = MIN_DIGIT - 1;
    final Value[] left = slice(members, -1, mid), right = slice(members, mid, ml);
    left[0] = head;
    return new BigArray(left, right, tp);
  }

  @Override
  public XQArray append(final Value last) {
    final Type tp = union(last);
    final int ml = members.length;
    if(ml < MAX_SMALL) {
      final Value[] newMembers = slice(members, 0, ml + 1);
      newMembers[newMembers.length - 1] = last;
      return new SmallArray(newMembers, tp);
    }

    final Value[] left = slice(members, 0, MIN_DIGIT), right = slice(members, MIN_DIGIT, ml + 1);
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
  public XQArray concat(final XQArray other) {
    return other.isEmptyArray() ? this : other.prepend(this);
  }

  @Override
  public Value head() {
    return members[0];
  }

  @Override
  public Value foot() {
    return members[members.length - 1];
  }

  @Override
  public XQArray trunk() {
    final int ml = members.length;
    return ml == 1 ? empty() :
           ml == 2 ? new SingletonArray(members[0]) :
           new SmallArray(slice(members, 0, ml - 1), type);
  }

  @Override
  public XQArray tail() {
    final int ml = members.length;
    return ml == 1 ? empty() :
           ml == 2 ? new SingletonArray(members[1]) :
           new SmallArray(slice(members, 1, ml), type);
  }

  @Override
  public boolean isEmptyArray() {
    return false;
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    qc.checkStop();
    final int ml = members.length;
    final Value[] tmp = new Value[ml];
    for(int m = 0; m < ml; m++) tmp[m] = members[ml - 1 - m];
    return new SmallArray(tmp, type);
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    qc.checkStop();
    final Type tp = union(value);
    final int ml = members.length, p = (int) pos;
    final Value[] out = new Value[ml + 1];
    Array.copy(members, p, out);
    out[p] = value;
    Array.copy(members, p, ml - p, out, p + 1);

    return ml < MAX_SMALL ? new SmallArray(out, tp) :
      new BigArray(slice(out, 0, MIN_DIGIT), slice(out, MIN_DIGIT, ml + 1), tp);
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int ml = members.length, p = (int) pos;
    if(ml == 2) return new SingletonArray(members[p == 0 ? 1 : 0]);

    final Value[] out = new Value[ml - 1];
    Array.copy(members, p, out);
    Array.copy(members, p + 1, ml - 1 - p, out, p);
    return new SmallArray(out, type);
  }

  @Override
  public XQArray subArray(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = (int) length;
    return n == 0 ? empty() :
           n == 1 ? new SingletonArray(members[p]) :
           new SmallArray(slice(members, p, p + n), type);
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    return new ListIterator<>() {
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
    final int ml = members.length;
    if(ml == 0) throw new AssertionError("Empty array in " + Util.className(this));
    if(ml > MAX_SMALL) throw new AssertionError("Array too big: " + ml);
  }

  @Override
  XQArray prepend(final SmallArray array) {
    final Type tp = type.union(array.type);
    final Value[] tmp = array.members;
    final int tl = tmp.length, ml = members.length, n = tl + ml;

    // both arrays can be used as digits
    if(Math.min(tl, ml) >= MIN_DIGIT) return new BigArray(tmp, members, tp);

    final Value[] out = new Value[n];
    Array.copy(tmp, tl, out);
    Array.copyFromStart(members, ml, out, tl);
    if(n <= MAX_SMALL) return new SmallArray(out, tp);

    final int mid = n / 2;
    return new BigArray(slice(out, 0, mid), slice(out, mid, n), tp);
  }
}
