package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This is an efficient and memory-saving hash map for storing items and values.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ItemValueMap extends HashItemSet {
  /** Map values. */
  private Value[] values;

  /**
   * Constructor with initial capacity.
   * @param capacity initial capacity (will be resized to a power of two)
   */
  public ItemValueMap(final long capacity) {
    super(Mode.ATOMIC, null, capacity);
    values = new Value[capacity()];
  }

  /**
   * Returns the value with the specified id.
   * All ids start with {@code 1} instead of {@code 0}.
   * @param id id of the value
   * @return value
   */
  public Value value(final int id) {
    return values[id];
  }

  /**
   * Stores the specified key and value.
   * If the key exists, the value is updated.
   * @param key key
   * @param value value
   * @throws QueryException query exception
   */
  public void put(final Item key, final Value value) throws QueryException {
    // array bounds are checked before array is resized
    final int i = put(key);
    values[i] = value;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@code null} if nothing was found
   * @throws QueryException query exception
   */
  public Value get(final Item key) throws QueryException {
    return values[id(key)];
  }

  @Override
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    values = Arrays.copyOf(values, newSize);
  }

  @Override
  public String toString() {
    return toString(keys, values);
  }
}
