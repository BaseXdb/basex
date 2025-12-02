package org.basex.util.hash;

import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens and objects.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <E> generic value type
 */
public final class TokenObjectMap<E> extends TokenSet {
  /** Values. */
  private Object[] values;

  /**
   * Default constructor.
   */
  public TokenObjectMap() {
    this(INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public TokenObjectMap(final long capacity) {
    super(capacity);
    values = new Object[capacity()];
  }

  /**
   * Stores the specified key and value. If the key exists, the value is updated.
   * @param key key
   * @param value value
   * @return old value (can be {@code null}
   */
  @SuppressWarnings("unchecked")
  public E put(final byte[] key, final E value) {
    // array bounds are checked before array is resized
    final int i = put(key);
    final E v = (E) values[i];
    values[i] = value;
    return v;
  }

  /**
   * Returns the value for the specified key. Creates a new value if none exists.
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
   * @return value, or {@code null} if the key does not exist
   */
  @SuppressWarnings("unchecked")
  public E get(final byte[] key) {
    return key != null ? (E) values[index(key)] : null;
  }

  /**
   * Returns the value with the specified index.
   * @param index index of the value (starts with {@code 1})
   * @return value
   */
  @SuppressWarnings("unchecked")
  public E value(final int index) {
    return (E) values[index];
  }

  /**
   * Assigns the value with the specified index.
   * @param value value to assign
   * @param index index of the value (starts with {@code 1})
   */
  public void value(final int index, final E value) {
    values[index] = value;
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
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    values = Array.copy(values, new Object[newSize]);
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(values, null);
  }

  @Override
  public String toString() {
    return toString(keys, values);
  }
}
