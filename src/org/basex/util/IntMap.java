package org.basex.util;

import java.util.Arrays;

/**
 * This is a simple hash map, extending the even simpler
 * {@link TokenSet hash set}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IntMap extends TokenSet {
  /** Values. */
  private int[] values = new int[CAP];

  /**
   * Indexes the specified keys and values.
   * If the entry exists, the old value is replaced.
   * @param key key
   * @param val value
   */
  public void add(final byte[] key, final int val) {
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Indexes the specified keys and values.
   * If the entry exists, the old value is replaced.
   * @param key key
   * @param val value
   */
  public void addNN(final byte[] key, final int val) {
    add(key, val + 1);
  }

  /**
   * Updates the value of the specified key.
   * @param key key
   * @param val value
   */
  public void set(final byte[] key, final int val) {
    final int i = id(key);
    if(i != 0) values[i] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or null if nothing was found
   */
  public int get(final byte[] key) {
    return key != null ? values[id(key)] : 0;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or -1 if nothing was found
   */
  public int getNN(final byte[] key) {
    if (key == null) return -1;
    final int id = id(key);
    return values[id] == 0 ? -1 : values[id] - 1;
  }

  
  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}
