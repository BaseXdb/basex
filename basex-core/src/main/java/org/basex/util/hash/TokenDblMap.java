package org.basex.util.hash;

import java.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens and doubles.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TokenDblMap extends TokenSet {
  /** Values of empty maps (shared: its single entry must never be assigned). */
  private static final double[] NO_VALUES = new double[1];

  /** Values. */
  private double[] values;

  /**
   * Default constructor (the hash table will be allocated when the first key is added).
   */
  public TokenDblMap() {
    values = NO_VALUES;
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public TokenDblMap(final long capacity) {
    super(capacity);
    values = new double[capacity()];
  }

  /**
   * Stores the specified key and value. If the key exists, the value is updated.
   * @param key key
   * @param value value
   */
  public void put(final byte[] key, final double value) {
    // the index must be resolved first: the array is reassigned if the map is resized
    final int i = put(key);
    values[i] = value;
  }

  /**
   * Returns the value with the specified index.
   * @param index index of the value (starts with {@code 1})
   * @return value
   */
  public double value(final int index) {
    return values[index];
  }

  @Override
  public int remove(final byte[] key) {
    final int i = super.remove(key);
    if(i != 0) values[i] = 0;
    return i;
  }

  @Override
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    values = Arrays.copyOf(values, newSize);
  }

  @Override
  public String toString() {
    final List<Object> v = new ArrayList<>();
    for(final double value : values) v.add(value);
    return toString(keys, v.toArray());
  }
}
