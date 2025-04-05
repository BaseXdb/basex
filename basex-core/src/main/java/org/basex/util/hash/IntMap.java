package org.basex.util.hash;

import java.util.*;

/**
 * This is an efficient and memory-saving hash map for storing primitive integers.
 * {@link Integer#MIN_VALUE} is returned for an entry that does not exist.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IntMap extends IntSet {
  /** Values. */
  private int[] values;

  /**
   * Default constructor.
   */
  public IntMap() {
    this(INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public IntMap(final long capacity) {
    super(capacity);
    values = new int[capacity()];
    values[0] = Integer.MIN_VALUE;
  }

  /**
   * Stores the specified key and value. If the key exists, the value is updated.
   * Note that {@link Integer#MIN_VALUE} is used to indicate that a key does not exist.
   * @param key key
   * @param value value
   * @return old value
   */
  public int put(final int key, final int value) {
    // array bounds are checked before array is resized
    final int i = put(key);
    final int v = values[i];
    values[i] = value;
    return v;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@link Integer#MIN_VALUE} if the key does not exist
   */
  public int get(final int key) {
    return values[index(key)];
  }

  /**
   * Returns the value with the specified index.
   * The index starts with {@code 1} instead of {@code 0}.
   * @param index index of the value
   * @return value
   */
  public int value(final int index) {
    return values[index];
  }

  @Override
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    values = Arrays.copyOf(values, newSize);
  }

  @Override
  public String toString() {
    final List<Object> k = new ArrayList<>(), v = new ArrayList<>();
    for(final int key : keys) k.add(key);
    for(final int value : values) v.add(value);
    return toString(k.toArray(), v.toArray());
  }
}
