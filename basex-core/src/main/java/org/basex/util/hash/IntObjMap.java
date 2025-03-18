package org.basex.util.hash;

import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing primitive integers and objects.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <E> generic value type
 */
public final class IntObjMap<E> extends IntSet {
  /** Values. */
  private Object[] values;

  /**
   * Default constructor.
   */
  public IntObjMap() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public IntObjMap(final long capacity) {
    super(capacity);
    values = new Object[capacity()];
  }

  /**
   * Stores the specified key and value.
   * If the key exists, the value is updated.
   * @param key key
   * @param value value
   * @return old value
   */
  @SuppressWarnings("unchecked")
  public E put(final int key, final E value) {
    // array bounds are checked before array is resized
    final int i = put(key);
    final E v = (E) values[i];
    values[i] = value;
    return v;
  }

  /**
   * Returns the value for the specified key.
   * Creates a new value if none exists.
   * @param key key
   * @param func function that create a new value
   * @return value
   */
  public E computeIfAbsent(final int key, final Supplier<? extends E> func) {
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
   * @return value or {@code null} if the key does not exist
   */
  @SuppressWarnings("unchecked")
  public E get(final int key) {
    return (E) values[id(key)];
  }

  /**
   * Returns the value with the specified id.
   * All ids start with {@code 1} instead of {@code 0}.
   * @param id id of the value
   * @return value
   */
  @SuppressWarnings("unchecked")
  public E value(final int id) {
    return (E) values[id];
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
  public void clear() {
    super.clear();
    Arrays.fill(values, null);
  }

  @Override
  public String toString() {
    final List<Object> k = new ArrayList<>();
    for(final int key : keys) k.add(key);
    return toString(k.toArray(), values);
  }
}
