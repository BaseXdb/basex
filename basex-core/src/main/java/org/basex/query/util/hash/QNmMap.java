package org.basex.query.util.hash;

import java.util.function.*;

import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing QNames and objects.
 * {@link QNmSet hash set}.
 *
 * @author BaseX Team, BSD License
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
   * Stores the specified key and value.
   * If the key exists, the value is updated.
   * @param key QName to look up
   * @param val value
   * @return old value
   */
  @SuppressWarnings("unchecked")
  public E put(final QNm key, final E val) {
    // array bounds are checked before array is resized
    final int i = put(key);
    final E v = (E) values[i];
    values[i] = val;
    return v;
  }

  /**
   * Returns the value for the specified key.
   * Creates a new value if none exists.
   * @param key key
   * @param func function that create a new value
   * @return value
   */
  public E computeIfAbsent(final QNm key, final Supplier<? extends E> func) {
    E value = get(key);
    if(value == null) {
      value = func.get();
      put(key, value);
    }
    return value;
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
