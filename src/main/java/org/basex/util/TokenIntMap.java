package org.basex.util;

import java.util.Arrays;

/**
 * This is a simple hash map, extending the even simpler
 * {@link TokenSet hash set}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class TokenIntMap extends TokenSet {
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
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or -1 if nothing was found
   */
  public int get(final byte[] key) {
    final int id = id(key);
    return id == 0 ? -1 : values[id];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}
