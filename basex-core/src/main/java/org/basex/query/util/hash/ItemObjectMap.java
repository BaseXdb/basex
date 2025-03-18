package org.basex.query.util.hash;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing items and values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <E> generic value type
 */
public final class ItemObjectMap<E> extends HashItemSet {
  /** Map values. */
  private Object[] values;

  /**
   * Constructor.
   */
  public ItemObjectMap() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity initial capacity (will be resized to a power of two)
   */
  public ItemObjectMap(final long capacity) {
    super(Mode.ATOMIC, null, capacity);
    values = new Object[capacity()];
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
   * Stores the specified key and value.
   * If the key exists, the value is updated.
   * @param key key
   * @param value value
   * @throws QueryException query exception
   * @return old value
   */
  @SuppressWarnings("unchecked")
  public E put(final Item key, final E value) throws QueryException {
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
   * @throws QueryException query exception
   */
  public E computeIfAbsent(final Item key, final Supplier<? extends E> func) throws QueryException {
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
   * @throws QueryException query exception
   */
  @SuppressWarnings("unchecked")
  public E get(final Item key) throws QueryException {
    return (E) values[id(key)];
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
