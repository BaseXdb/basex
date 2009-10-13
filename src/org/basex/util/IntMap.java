package org.basex.util;

/**
 * This is a simple hash map, extending the even simpler
 * {@link Set hash set}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IntMap extends Set {
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
   * Update the old value of the entry.
   * @param key key
   * @param val value
   */
  public void set(final byte[] key, final int val) {
    final int i = id(key);
    if(i != 0) values[Math.abs(i)] = val;
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
   * Returns the specified value.
   * @param p value index
   * @return value
   */
  public int value(final int p) {
    return values[p];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Array.extend(values);
  }
}
