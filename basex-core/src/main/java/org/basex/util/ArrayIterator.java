package org.basex.util;

import java.util.*;

/**
 * This class is used to iterate through objects of an array.
 * @param <E> generic object type
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArrayIterator<E> implements Iterator<E>, Iterable<E> {
  /** Array. */
  private final Object[] a;
  /** Index + 1 of last object to return. */
  private final int e;
  /** Current index. */
  private int s;

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
    a = array;
    s = start;
    e = end;
  }

  @Override
  public Iterator<E> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    return s < e;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E next() {
    return (E) a[s++];
  }

  @Override
  public void remove() {
    throw Util.notExpected();
  }
}
