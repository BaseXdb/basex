package org.basex.util.hash;

import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens and objects.
 * {@link TokenSet hash set}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <E> generic value type
 */
public final class TokenObjMap<E> extends TokenSet {
  /** Values. */
  private Object[] values;

  /**
   * Default constructor.
   */
  public TokenObjMap() {
    values = new Object[capacity()];
  }

  /**
   * Indexes the specified key and value.
   * If the key exists, the value is updated.
   * @param key key
   * @param val value
   */
  public void put(final byte[] key, final E val) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    values[i] = val;
  }

  /**
   * Returns the value for the specified key.
   * Creates a new value if none exists.
   * @param key key
   * @param func function that create a new value
   * @return value
   */
  public E computeIfAbsent(final byte[] key, final Supplier<? extends E> func) {
    E value = get(key);
    if(value == null) {
      value = func.get();
      put(key, value);
    }
    return value;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@code null} if nothing was found
   */
  @SuppressWarnings("unchecked")
  public E get(final byte[] key) {
    return key != null ? (E) values[id(key)] : null;
  }

  /**
   * Returns a value iterator.
   * @return iterator
   */
  public Iterable<E> values() {
    return new ArrayIterator<>(values, 1, size);
  }

  @Override
  public int remove(final byte[] key) {
    final int i = super.remove(key);
    values[i] = null;
    return i;
  }

  @Override
  public void clear() {
    Arrays.fill(values, null);
    super.clear();
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
