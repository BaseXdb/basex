package org.basex.query.value.array;

import java.util.*;

import org.basex.query.value.*;

/**
 * The empty array.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
final class EmptyArray extends Array {
  /** The empty array. */
  static final EmptyArray INSTANCE = new EmptyArray();

  /** Hidden constructor. */
  private EmptyArray() {
  }

  @Override
  public Array cons(final Value elem) {
    return new SmallArray(new Value[] { elem });
  }

  @Override
  public Array snoc(final Value elem) {
    return new SmallArray(new Value[] { elem });
  }

  @Override
  public Value get(final long index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public long arraySize() {
    return 0;
  }

  @Override
  public Array concat(final Array seq) {
    return seq;
  }

  @Override
  public Value head() {
    throw new NoSuchElementException();
  }

  @Override
  public Value last() {
    throw new NoSuchElementException();
  }

  @Override
  public Array init() {
    throw new IllegalStateException();
  }

  @Override
  public Array tail() {
    throw new IllegalStateException();
  }

  @Override
  public Array subArray(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    if(pos + len > 0)
      throw new IndexOutOfBoundsException("end out of bounds: " + (pos + len) + " > 0");
    return this;
  }

  @Override
  public boolean isEmptyArray() {
    return true;
  }

  @Override
  public Array reverseArray() {
    return this;
  }

  @Override
  public Array insertBefore(final long pos, final Value val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > 0) throw new IndexOutOfBoundsException("position too big: " + pos);
    return new SmallArray(new Value[] { val });
  }

  @Override
  public Array remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    throw new IndexOutOfBoundsException("position too big: " + pos);
  }

  @Override
  public ListIterator<Value> iterator(final long size) {
    return Collections.emptyListIterator();
  }

  @Override
  void checkInvariants() {
    // nothing can go wrong
  }

  @Override
  Array consSmall(final Value[] vals) {
    return new SmallArray(vals);
  }
}
