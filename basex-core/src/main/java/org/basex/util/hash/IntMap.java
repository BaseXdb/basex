package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing primitive integers.
 * It extends the {@link IntSet} class. All values except for {@link Integer#MIN_VALUE}
 * can be stored as values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IntMap extends IntSet {
  /** Values. */
  private int[] values;

  /**
   * Default constructor.
   */
  public IntMap() {
    this(Array.INITIAL_CAPACITY);
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
   * Indexes the specified key and stores the associated value.
   * If the key already exists, the value is updated.
   * Please note that the value {@link Integer#MIN_VALUE} cannot be stored.
   * @param key key
   * @param value value
   * @return old value
   */
  public int put(final int key, final int value) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    final int v = values[i];
    values[i] = value;
    return v;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value or {@link Integer#MIN_VALUE} if the key was not found
   */
  public int get(final int key) {
    return values[id(key)];
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
