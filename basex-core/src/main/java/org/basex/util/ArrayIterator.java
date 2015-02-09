package org.basex.util;

import java.util.*;

/**
 * This class is used to iterate through objects of an array.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @param <E> generic object type
 */
public final class ArrayIterator<E> implements Iterator<E>, Iterable<E> {
  /** Array. */
  private final Object[] array;
  /** Index + 1 of last object to return. */
  private final int end;
  /** Current index. */
  private int start;

  /**
   * Constructor.
   * @param array array to iterate through
   * @param end index + 1 of last object to return
   */
  public ArrayIterator(final Object[] array, final int end) {
    this(array, 0, end);
  }

  /**
   * Constructor.
   * @param array array to iterate through
   * @param start index of first object to return
   * @param end index + 1 of last object to return
   */
  public ArrayIterator(final Object[] array, final int start, final int end) {
    this.array = array;
    this.start = start;
    this.end = end;
  }

  @Override
  public Iterator<E> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    return start < end;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E next() {
    return (E) array[start++];
  }

  @Override
  public void remove() {
    throw Util.notExpected();
  }
}
