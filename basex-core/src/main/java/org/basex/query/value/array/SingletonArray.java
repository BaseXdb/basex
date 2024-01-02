package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A singleton array.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class SingletonArray extends XQArray {
  /** Single member. */
  final Value member;

  /**
   * Constructor.
   * @param member member
   */
  SingletonArray(final Value member) {
    super(ArrayType.get(member.seqType()));
    this.member = member;
  }

  @Override
  public XQArray prepend(final Value head) {
    return new SmallArray(new Value[] { head, member }, union(head));
  }

  @Override
  public XQArray append(final Value last) {
    return new SmallArray(new Value[] { member, last }, union(last));
  }

  @Override
  public Value get(final long index) {
    return member;
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    return new SingletonArray(value);
  }

  @Override
  public long arraySize() {
    return 1;
  }

  @Override
  public XQArray concat(final XQArray other) {
    return other.isEmptyArray() ? this : other.prepend(member);
  }

  @Override
  public Value head() {
    return member;
  }

  @Override
  public Value foot() {
    return member;
  }

  @Override
  public XQArray trunk() {
    return empty();
  }

  @Override
  public XQArray tail() {
    return empty();
  }

  @Override
  public boolean isEmptyArray() {
    return false;
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return this;
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    return pos == 0 ? prepend(value) : append(value);
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    return empty();
  }

  @Override
  public XQArray subArray(final long pos, final long length, final QueryContext qc) {
    return length == 0 ? empty() : this;
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
        return index < 1;
      }

      @Override
      public Value next() {
        ++index;
        return member;
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
        --index;
        return member;
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
  }

  @Override
  XQArray prepend(final SmallArray array) {
    return array.append(member);
  }
}
