package org.basex.query.util.hash;

import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing QNames and objects.
 * {@link QNmSet hash set}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <E> generic value type
 */
public final class QNmMap<E> extends QNmSet {
  /** Values. */
  private Object[] values;

  /**
   * Default constructor.
   */
  public QNmMap() {
    values = new Object[capacity()];
  }

  /**
   * Indexes the specified key and value.
   * If the key exists, the value is updated.
   * @param qnm QName to look up
   * @param val value
   */
  public void put(final QNm qnm, final E val) {
    // array bounds are checked before array is resized..
    final int i = put(qnm);
    values[i] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param qnm QName to look up
   * @return value, or {@code null} if nothing was found
   */
  @SuppressWarnings("unchecked")
  public E get(final QNm qnm) {
    return (E) values[id(qnm)];
  }

  /**
   * Returns a value iterator.
   * @return iterator
   */
  public Iterable<E> values() {
    return new ArrayIterator<>(values, 1, size);
  }

  @Override
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    values = Array.copy(values, new Object[newSize]);
  }

  @Override
  public String toString() {
    return toString(keys, values);
  }
}
